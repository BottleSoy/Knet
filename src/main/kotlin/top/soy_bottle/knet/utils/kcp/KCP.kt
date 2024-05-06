package top.soy_bottle.knet.utils.kcp


abstract class KCP(conv_: Long) {
	//=====================================================================
	// KCP BASIC
	//=====================================================================
	val IKCP_RTO_NDL: Int = 30 // no delay min rto
	val IKCP_RTO_MIN: Int = 100 // normal min rto
	val IKCP_RTO_DEF: Int = 200
	val IKCP_RTO_MAX: Int = 60000
	val IKCP_CMD_PUSH: Int = 81 // cmd: push data
	val IKCP_CMD_ACK: Int = 82 // cmd: ack
	val IKCP_CMD_WASK: Int = 83 // cmd: window probe (ask)
	val IKCP_CMD_WINS: Int = 84 // cmd: window size (tell)
	val IKCP_ASK_SEND: Int = 1 // need to send IKCP_CMD_WASK
	val IKCP_ASK_TELL: Int = 2 // need to send IKCP_CMD_WINS
	val IKCP_WND_SND: Int = 32
	val IKCP_WND_RCV: Int = 32
	val IKCP_MTU_DEF: Int = 1400
	val IKCP_ACK_FAST: Int = 3
	val IKCP_INTERVAL: Int = 100
	val IKCP_OVERHEAD: Int = 24
	val IKCP_DEADLINK: Int = 10
	val IKCP_THRESH_INIT: Int = 2
	val IKCP_THRESH_MIN: Int = 2
	val IKCP_PROBE_INIT: Int = 7000 // 7 secs to probe window size
	val IKCP_PROBE_LIMIT: Int = 120000 // up to 120 secs to probe window
	
	protected abstract fun output(buffer: ByteArray, size: Int) // 需具体实现
	
	inner class Segment(size: Int) {
		var conv: Long = 0
		var cmd: Long = 0
		var frg: Long = 0
		var wnd: Long = 0
		var ts: Long = 0
		var sn: Long = 0
		var una: Long = 0
		var resendts: Long = 0
		var rto: Long = 0
		var fastack: Long = 0
		var xmit: Long = 0
		var data: ByteArray = ByteArray(size)
		
		//---------------------------------------------------------------------
		// ikcp_encode_seg
		//---------------------------------------------------------------------
		// encode a segment into buffer
		fun encode(ptr: ByteArray, offset: Int): Int {
			var offset = offset
			val offset_ = offset
			
			ikcp_encode32u(ptr, offset, conv)
			offset += 4
			ikcp_encode8u(ptr, offset, cmd.toByte())
			offset += 1
			ikcp_encode8u(ptr, offset, frg.toByte())
			offset += 1
			ikcp_encode16u(ptr, offset, wnd.toInt())
			offset += 2
			ikcp_encode32u(ptr, offset, ts)
			offset += 4
			ikcp_encode32u(ptr, offset, sn)
			offset += 4
			ikcp_encode32u(ptr, offset, una)
			offset += 4
			ikcp_encode32u(ptr, offset, data.size.toLong())
			offset += 4
			
			return offset - offset_
		}
	}
	
	var conv: Long = 0
	
	//long user = user;
	var snd_una: Long = 0
	var snd_nxt: Long = 0
	var rcv_nxt: Long = 0
	var ts_recent: Long = 0
	var ts_lastack: Long = 0
	var ts_probe: Long = 0
	var probe_wait: Long = 0
	var snd_wnd: Long = IKCP_WND_SND.toLong()
	var rcv_wnd: Long = IKCP_WND_RCV.toLong()
	var rmt_wnd: Long = IKCP_WND_RCV.toLong()
	var cwnd: Long = 0
	var incr: Long = 0
	var probe: Long = 0
	var mtu: Long = IKCP_MTU_DEF.toLong()
	var mss: Long = this.mtu - IKCP_OVERHEAD
	var buffer: ByteArray = ByteArray((mtu + IKCP_OVERHEAD).toInt() * 3)
	var nrcv_buf: ArrayList<Segment> = ArrayList(128)
	var nsnd_buf: ArrayList<Segment> = ArrayList(128)
	var nrcv_que: ArrayList<Segment> = ArrayList(128)
	var nsnd_que: ArrayList<Segment> = ArrayList(128)
	var state: Long = 0
	var acklist: ArrayList<Long> = ArrayList(128)
	
	//long ackblock = 0;
	//long ackcount = 0;
	var rx_srtt: Long = 0
	var rx_rttval: Long = 0
	var rx_rto: Long = IKCP_RTO_DEF.toLong()
	var rx_minrto: Long = IKCP_RTO_MIN.toLong()
	var current: Long = 0
	var interval: Long = IKCP_INTERVAL.toLong()
	var ts_flush: Long = IKCP_INTERVAL.toLong()
	var nodelay: Long = 0
	var updated: Long = 0
	var logmask: Long = 0
	var ssthresh: Long = IKCP_THRESH_INIT.toLong()
	var fastresend: Long = 0
	var nocwnd: Long = 0
	var xmit: Long = 0
	var dead_link: Long = IKCP_DEADLINK.toLong()
	
	//long output = NULL;
	//long writelog = NULL;
	init {
		conv = conv_
	}
	
	//---------------------------------------------------------------------
	// user/upper level recv: returns size, returns below zero for EAGAIN
	//---------------------------------------------------------------------
	// 将接收队列中的数据传递给上层引用
	fun Recv(buffer: ByteArray, off: Int, len: Int): Int {
		if (0 == nrcv_que.size) {
			return -1
		}
		
		val peekSize = peekSize()
		if (0 > peekSize) {
			return -2
		}
		
		if (peekSize > len) {
			return -3
		}
		
		var recover = false
		if (nrcv_que.size >= rcv_wnd) {
			recover = true
		}
		
		// merge fragment.
		var count = 0
		var n = 0
		for (seg in nrcv_que) {
			System.arraycopy(seg.data, off, buffer, n, seg.data.size)
			n += seg.data.size
			count++
			if (0L == seg.frg) {
				break
			}
		}
		
		if (0 < count) {
			slice(nrcv_que, count, nrcv_que.size)
		}
		
		// move available data from rcv_buf -> nrcv_que
		count = 0
		for (seg in nrcv_buf) {
			if (seg.sn == rcv_nxt && nrcv_que.size < rcv_wnd) {
				nrcv_que.add(seg)
				rcv_nxt++
				count++
			} else {
				break
			}
		}
		
		if (0 < count) {
			slice(nrcv_buf, count, nrcv_buf.size)
		}
		
		// fast recover
		if (nrcv_que.size < rcv_wnd && recover) {
			// ready to send back IKCP_CMD_WINS in ikcp_flush
			// tell remote my window size
			probe = probe or IKCP_ASK_TELL.toLong()
		}
		
		return n
	}
	
	//---------------------------------------------------------------------
	// peek data size
	//---------------------------------------------------------------------
	// check the size of next message in the recv queue
	// 计算接收队列中有多少可用的数据
	fun peekSize(): Int {
		if (0 == nrcv_que.size) {
			return -1
		}
		
		val seq = nrcv_que[0]
		
		if (0L == seq.frg) {
			return seq.data.size
		}
		
		if (nrcv_que.size < seq.frg + 1) {
			return -1
		}
		
		var length = 0
		
		for (item in nrcv_que) {
			length += item.data.size
			if (0L == item.frg) {
				break
			}
		}
		
		return length
	}
	
	//---------------------------------------------------------------------
	// user/upper level send, returns below zero for error
	//---------------------------------------------------------------------
	// 上层要发送的数据丢给发送队列，发送队列会根据mtu大小分片
	fun Send(buffer: ByteArray): Int {
		if (buffer.isEmpty()) {
			return -1
		}
		
		var count: Int
		
		// 根据mss大小分片
		count = if (buffer.size < mss) {
			1
		} else {
			(buffer.size + mss - 1).toInt() / mss.toInt()
		}
		
		if (255 < count) {
			return -2
		}
		
		if (0 == count) {
			count = 1
		}
		
		var offset = 0
		
		// 分片后加入到发送队列
		var length = buffer.size
		for (i in 0 until count) {
			val size = (if (length > mss) mss else length.toLong()).toInt()
			val seg: Segment = Segment(size)
			System.arraycopy(buffer, offset, seg.data, 0, size)
			offset += size
			seg.frg = (count - i - 1).toLong()
			nsnd_que.add(seg)
			length -= size
		}
		return 0
	}
	
	//---------------------------------------------------------------------
	// parse ack
	//---------------------------------------------------------------------
	fun update_ack(rtt: Int) {
		if (0L == rx_srtt) {
			rx_srtt = rtt.toLong()
			rx_rttval = (rtt / 2).toLong()
		} else {
			var delta = (rtt - rx_srtt).toInt()
			if (0 > delta) {
				delta = -delta
			}
			
			rx_rttval = (3 * rx_rttval + delta) / 4
			rx_srtt = (7 * rx_srtt + rtt) / 8
			if (rx_srtt < 1) {
				rx_srtt = 1
			}
		}
		
		val rto = (rx_srtt + _imax_(1, 4 * rx_rttval)).toInt()
		rx_rto = _ibound_(rx_minrto, rto.toLong(), IKCP_RTO_MAX.toLong())
	}
	
	// 计算本地真实snd_una
	fun shrink_buf() {
		snd_una = if (nsnd_buf.size > 0) {
			nsnd_buf[0].sn
		} else {
			snd_nxt
		}
	}
	
	// 对端返回的ack, 确认发送成功时，对应包从发送缓存中移除
	fun parse_ack(sn: Long) {
		if (_itimediff(sn, snd_una) < 0 || _itimediff(sn, snd_nxt) >= 0) {
			return
		}
		
		var index = 0
		for (seg in nsnd_buf) {
			if (_itimediff(sn, seg.sn) < 0) {
				break
			}
			
			// 原版ikcp_parse_fastack&ikcp_parse_ack逻辑重复
			seg.fastack++
			
			if (sn == seg.sn) {
				nsnd_buf.removeAt(index)
				break
			}
			index++
		}
	}
	
	// 通过对端传回的una将已经确认发送成功包从发送缓存中移除
	fun parse_una(una: Long) {
		var count = 0
		for (seg in nsnd_buf) {
			if (_itimediff(una, seg.sn) > 0) {
				count++
			} else {
				break
			}
		}
		
		if (0 < count) {
			slice(nsnd_buf, count, nsnd_buf.size)
		}
	}
	
	//---------------------------------------------------------------------
	// ack append
	//---------------------------------------------------------------------
	// 收数据包后需要给对端回ack，flush时发送出去
	fun ack_push(sn: Long, ts: Long) {
		// c原版实现中按*2扩大容量
		acklist.add(sn)
		acklist.add(ts)
	}
	
	//---------------------------------------------------------------------
	// parse data
	//---------------------------------------------------------------------
	// 用户数据包解析
	fun parse_data(newseg: Segment) {
		val sn = newseg.sn
		var repeat = false
		
		if (_itimediff(sn, rcv_nxt + rcv_wnd) >= 0 || _itimediff(sn, rcv_nxt) < 0) {
			return
		}
		
		val n = nrcv_buf.size - 1
		var after_idx = -1
		
		// 判断是否是重复包，并且计算插入位置
		for (i in n downTo 0) {
			val seg = nrcv_buf[i]
			if (seg.sn == sn) {
				repeat = true
				break
			}
			
			if (_itimediff(sn, seg.sn) > 0) {
				after_idx = i
				break
			}
		}
		
		// 如果不是重复包，则插入
		if (!repeat) {
			if (after_idx == -1) {
				nrcv_buf.add(0, newseg)
			} else {
				nrcv_buf.add(after_idx + 1, newseg)
			}
		}
		
		// move available data from nrcv_buf -> nrcv_que
		// 将连续包加入到接收队列
		var count = 0
		for (seg in nrcv_buf) {
			if (seg.sn == rcv_nxt && nrcv_que.size < rcv_wnd) {
				nrcv_que.add(seg)
				rcv_nxt++
				count++
			} else {
				break
			}
		}
		
		// 从接收缓存中移除
		if (0 < count) {
			slice(nrcv_buf, count, nrcv_buf.size)
		}
	}
	
	// when you received a low level packet (eg. UDP packet), call it
	//---------------------------------------------------------------------
	// input data
	//---------------------------------------------------------------------
	// 底层收包后调用，再由上层通过Recv获得处理后的数据
	fun Input(data: ByteArray): Int {
		val s_una = snd_una
		if (data.size < IKCP_OVERHEAD) {
			return 0
		}
		
		var offset = 0
		
		while (true) {
			var ts: Long
			var sn: Long
			var una: Long
			var wnd: Int
			var cmd: Byte
			var frg: Byte
			
			if (data.size - offset < IKCP_OVERHEAD) {
				break
			}
			
			var conv_ = ikcp_decode32u(data, offset)
			offset += 4
			if (conv != conv_) {
				return -1
			}
			
			cmd = ikcp_decode8u(data, offset)
			offset += 1
			frg = ikcp_decode8u(data, offset)
			offset += 1
			wnd = ikcp_decode16u(data, offset)
			offset += 2
			ts = ikcp_decode32u(data, offset)
			offset += 4
			sn = ikcp_decode32u(data, offset)
			offset += 4
			una = ikcp_decode32u(data, offset)
			offset += 4
			var length = ikcp_decode32u(data, offset)
			offset += 4
			
			if (data.size - offset < length) {
				return -2
			}
			
			if (cmd.toInt() != IKCP_CMD_PUSH && cmd.toInt() != IKCP_CMD_ACK && cmd.toInt() != IKCP_CMD_WASK && cmd.toInt() != IKCP_CMD_WINS) {
				return -3
			}
			
			rmt_wnd = wnd.toLong()
			parse_una(una)
			shrink_buf()
			
			if (IKCP_CMD_ACK == cmd.toInt()) {
				if (_itimediff(current, ts) >= 0) {
					update_ack(_itimediff(current, ts))
				}
				parse_ack(sn)
				shrink_buf()
			} else if (IKCP_CMD_PUSH == cmd.toInt()) {
				if (_itimediff(sn, rcv_nxt + rcv_wnd) < 0) {
					ack_push(sn, ts)
					if (_itimediff(sn, rcv_nxt) >= 0) {
						val seg: Segment = Segment(length.toInt())
						seg.conv = conv_
						seg.cmd = cmd.toLong()
						seg.frg = frg.toLong()
						seg.wnd = wnd.toLong()
						seg.ts = ts
						seg.sn = sn
						seg.una = una
						
						if (length > 0) {
							System.arraycopy(data, offset, seg.data, 0, length.toInt())
						}
						
						parse_data(seg)
					}
				}
			} else if (IKCP_CMD_WASK == cmd.toInt()) {
				// ready to send back IKCP_CMD_WINS in Ikcp_flush
				// tell remote my window size
				probe = probe or IKCP_ASK_TELL.toLong()
			} else if (IKCP_CMD_WINS == cmd.toInt()) {
				// do nothing
			} else {
				return -3
			}
			
			offset += length.toInt()
		}
		
		if (_itimediff(snd_una, s_una) > 0) {
			if (cwnd < rmt_wnd) {
				val mss_ = mss
				if (cwnd < ssthresh) {
					cwnd++
					incr += mss_
				} else {
					if (incr < mss_) {
						incr = mss_
					}
					incr += (mss_ * mss_) / incr + (mss_ / 16)
					if ((cwnd + 1) * mss_ <= incr) {
						cwnd++
					}
				}
				if (cwnd > rmt_wnd) {
					cwnd = rmt_wnd
					incr = rmt_wnd * mss_
				}
			}
		}
		
		return 0
	}
	
	// 接收窗口可用大小
	fun wnd_unused(): Int {
		if (nrcv_que.size < rcv_wnd) {
			return rcv_wnd.toInt() - nrcv_que.size
		}
		return 0
	}
	
	//---------------------------------------------------------------------
	// ikcp_flush
	//---------------------------------------------------------------------
	fun flush() {
		val current_ = current
		val buffer_ = buffer
		var change = 0
		var lost = 0
		
		// 'ikcp_update' haven't been called.
		if (0L == updated) {
			return
		}
		
		val seg: Segment = Segment(0)
		seg.conv = conv
		seg.cmd = IKCP_CMD_ACK.toLong()
		seg.wnd = wnd_unused().toLong()
		seg.una = rcv_nxt
		
		// flush acknowledges
		// 将acklist中的ack发送出去
		var count = acklist.size / 2
		var offset = 0
		for (i in 0 until count) {
			if (offset + IKCP_OVERHEAD > mtu) {
				output(buffer, offset)
				offset = 0
			}
			// ikcp_ack_get
			seg.sn = acklist[i * 2 + 0]
			seg.ts = acklist[i * 2 + 1]
			offset += seg.encode(buffer, offset)
		}
		acklist.clear()
		
		// probe window size (if remote window size equals zero)
		// rmt_wnd=0时，判断是否需要请求对端接收窗口
		if (0L == rmt_wnd) {
			if (0L == probe_wait) {
				probe_wait = IKCP_PROBE_INIT.toLong()
				ts_probe = current + probe_wait
			} else {
				// 逐步扩大请求时间间隔
				if (_itimediff(current, ts_probe) >= 0) {
					if (probe_wait < IKCP_PROBE_INIT) {
						probe_wait = IKCP_PROBE_INIT.toLong()
					}
					probe_wait += probe_wait / 2
					if (probe_wait > IKCP_PROBE_LIMIT) {
						probe_wait = IKCP_PROBE_LIMIT.toLong()
					}
					ts_probe = current + probe_wait
					probe = probe or IKCP_ASK_SEND.toLong()
				}
			}
		} else {
			ts_probe = 0
			probe_wait = 0
		}
		
		// flush window probing commands
		// 请求对端接收窗口
		if ((probe and IKCP_ASK_SEND.toLong()) != 0L) {
			seg.cmd = IKCP_CMD_WASK.toLong()
			if (offset + IKCP_OVERHEAD > mtu) {
				output(buffer, offset)
				offset = 0
			}
			offset += seg.encode(buffer, offset)
		}
		
		// flush window probing commands(c#)
		// 告诉对端自己的接收窗口
		if ((probe and IKCP_ASK_TELL.toLong()) != 0L) {
			seg.cmd = IKCP_CMD_WINS.toLong()
			if (offset + IKCP_OVERHEAD > mtu) {
				output(buffer, offset)
				offset = 0
			}
			offset += seg.encode(buffer, offset)
		}
		
		probe = 0
		
		// calculate window size
		var cwnd_ = _imin_(snd_wnd, rmt_wnd)
		// 如果采用拥塞控制
		if (0L == nocwnd) {
			cwnd_ = _imin_(cwnd, cwnd_)
		}
		
		count = 0
		// move data from snd_queue to snd_buf
		for (nsnd_que1 in nsnd_que) {
			if (_itimediff(snd_nxt, snd_una + cwnd_) >= 0) {
				break
			}
			val newseg = nsnd_que1
			newseg.conv = conv
			newseg.cmd = IKCP_CMD_PUSH.toLong()
			newseg.wnd = seg.wnd
			newseg.ts = current_
			newseg.sn = snd_nxt
			newseg.una = rcv_nxt
			newseg.resendts = current_
			newseg.rto = rx_rto
			newseg.fastack = 0
			newseg.xmit = 0
			nsnd_buf.add(newseg)
			snd_nxt++
			count++
		}
		
		if (0 < count) {
			slice(nsnd_que, count, nsnd_que.size)
		}
		
		// calculate resent
		val resent = if ((fastresend > 0)) fastresend else -0x1
		val rtomin = if ((nodelay == 0L)) (rx_rto shr 3) else 0
		
		// flush data segments
		for (segment in nsnd_buf) {
			var needsend = false
			if (0L == segment.xmit) {
				// 第一次传输
				needsend = true
				segment.xmit++
				segment.rto = rx_rto
				segment.resendts = current_ + segment.rto + rtomin
			} else if (_itimediff(current_, segment.resendts) >= 0) {
				// 丢包重传
				needsend = true
				segment.xmit++
				xmit++
				if (0L == nodelay) {
					segment.rto += rx_rto
				} else {
					segment.rto += rx_rto / 2
				}
				segment.resendts = current_ + segment.rto
				lost = 1
			} else if (segment.fastack >= resent) {
				// 快速重传
				needsend = true
				segment.xmit++
				segment.fastack = 0
				segment.resendts = current_ + segment.rto
				change++
			}
			
			if (needsend) {
				segment.ts = current_
				segment.wnd = seg.wnd
				segment.una = rcv_nxt
				
				val need = IKCP_OVERHEAD + segment.data.size
				if (offset + need >= mtu) {
					output(buffer, offset)
					offset = 0
				}
				
				offset += segment.encode(buffer, offset)
				if (segment.data.isNotEmpty()) {
					System.arraycopy(segment.data, 0, buffer, offset, segment.data.size)
					offset += segment.data.size
				}
				
				if (segment.xmit >= dead_link) {
					state = -1 // state = 0(c#)
				}
			}
		}
		
		// flash remain segments
		if (offset > 0) {
			output(buffer, offset)
		}
		
		// update ssthresh
		// 拥塞避免
		if (change != 0) {
			val inflight = snd_nxt - snd_una
			ssthresh = inflight / 2
			if (ssthresh < IKCP_THRESH_MIN) {
				ssthresh = IKCP_THRESH_MIN.toLong()
			}
			cwnd = ssthresh + resent
			incr = cwnd * mss
		}
		
		if (lost != 0) {
			ssthresh = cwnd / 2
			if (ssthresh < IKCP_THRESH_MIN) {
				ssthresh = IKCP_THRESH_MIN.toLong()
			}
			cwnd = 1
			incr = mss
		}
		
		if (cwnd < 1) {
			cwnd = 1
			incr = mss
		}
	}
	
	//---------------------------------------------------------------------
	// update state (call it repeatedly, every 10ms-100ms), or you can ask
	// ikcp_check when to call it again (without ikcp_input/_send calling).
	// 'current' - current timestamp in millisec.
	//---------------------------------------------------------------------
	fun Update(current_: Long) {
		current = current_
		
		// 首次调用Update
		if (0L == updated) {
			updated = 1
			ts_flush = current
		}
		
		// 两次更新间隔
		var slap = _itimediff(current, ts_flush)
		
		// interval设置过大或者Update调用间隔太久
		if (slap >= 10000 || slap < -10000) {
			ts_flush = current
			slap = 0
		}
		
		// flush同时设置下一次更新时间
		if (slap >= 0) {
			ts_flush += interval
			if (_itimediff(current, ts_flush) >= 0) {
				ts_flush = current + interval
			}
			flush()
		}
	}
	
	//---------------------------------------------------------------------
	// Determine when should you invoke ikcp_update:
	// returns when you should invoke ikcp_update in millisec, if there
	// is no ikcp_input/_send calling. you can call ikcp_update in that
	// time, instead of call update repeatly.
	// Important to reduce unnacessary ikcp_update invoking. use it to
	// schedule ikcp_update (eg. implementing an epoll-like mechanism,
	// or optimize ikcp_update when handling massive kcp connections)
	//---------------------------------------------------------------------
	fun Check(current_: Long): Long {
		var ts_flush_ = ts_flush
		var tm_flush: Long = 0x7fffffff
		var tm_packet: Long = 0x7fffffff
		var minimal: Long
		
		if (0L == updated) {
			return current_
		}
		
		if (_itimediff(current_, ts_flush_) >= 10000 || _itimediff(current_, ts_flush_) < -10000) {
			ts_flush_ = current_
		}
		
		if (_itimediff(current_, ts_flush_) >= 0) {
			return current_
		}
		
		tm_flush = _itimediff(ts_flush_, current_).toLong()
		
		for (seg in nsnd_buf) {
			val diff = _itimediff(seg.resendts, current_)
			if (diff <= 0) {
				return current_
			}
			if (diff < tm_packet) {
				tm_packet = diff.toLong()
			}
		}
		
		minimal = if (tm_packet < tm_flush) tm_packet else tm_flush
		if (minimal >= interval) {
			minimal = interval
		}
		
		return current_ + minimal
	}
	
	// change MTU size, default is 1400
	fun SetMtu(mtu_: Int): Int {
		if (mtu_ < 50 || mtu_ < IKCP_OVERHEAD) {
			return -1
		}
		
		val buffer_ = ByteArray((mtu_ + IKCP_OVERHEAD) * 3) ?: return -2
		
		mtu = mtu_.toLong()
		mss = mtu - IKCP_OVERHEAD
		buffer = buffer_
		return 0
	}
	
	fun Interval(interval_: Int): Int {
		var interval_ = interval_
		if (interval_ > 5000) {
			interval_ = 5000
		} else if (interval_ < 10) {
			interval_ = 10
		}
		interval = interval_.toLong()
		return 0
	}
	
	// fastest: ikcp_nodelay(kcp, 1, 20, 2, 1)
	// nodelay: 0:disable(default), 1:enable
	// interval: internal update timer interval in millisec, default is 100ms
	// resend: 0:disable fast resend(default), 1:enable fast resend
	// nc: 0:normal congestion control(default), 1:disable congestion control
	fun NoDelay(nodelay_: Int, interval_: Int, resend_: Int, nc_: Int): Int {
		var interval_ = interval_
		if (nodelay_ >= 0) {
			nodelay = nodelay_.toLong()
			rx_minrto = if (nodelay_ != 0) {
				IKCP_RTO_NDL.toLong()
			} else {
				IKCP_RTO_MIN.toLong()
			}
		}
		
		if (interval_ >= 0) {
			if (interval_ > 5000) {
				interval_ = 5000
			} else if (interval_ < 10) {
				interval_ = 10
			}
			interval = interval_.toLong()
		}
		
		if (resend_ >= 0) {
			fastresend = resend_.toLong()
		}
		
		if (nc_ >= 0) {
			nocwnd = nc_.toLong()
		}
		
		return 0
	}
	
	// set maximum window size: sndwnd=32, rcvwnd=32 by default
	fun WndSize(sndwnd: Int, rcvwnd: Int): Int {
		if (sndwnd > 0) {
			snd_wnd = sndwnd.toLong()
		}
		
		if (rcvwnd > 0) {
			rcv_wnd = rcvwnd.toLong()
		}
		return 0
	}
	
	// get how many packet is waiting to be sent
	fun WaitSnd(): Int {
		return nsnd_buf.size + nsnd_que.size
	}
	
	companion object {
		// encode 8 bits unsigned int
		fun ikcp_encode8u(p: ByteArray, offset: Int, c: Byte) {
			p[0 + offset] = c
		}
		
		// decode 8 bits unsigned int
		fun ikcp_decode8u(p: ByteArray, offset: Int): Byte {
			return p[0 + offset]
		}
		
		/* encode 16 bits unsigned int (msb) */
		fun ikcp_encode16u(p: ByteArray, offset: Int, w: Int) {
			p[offset + 0] = (w shr 8).toByte()
			p[offset + 1] = (w shr 0).toByte()
		}
		
		/* decode 16 bits unsigned int (msb) */
		fun ikcp_decode16u(p: ByteArray, offset: Int): Int {
			val ret = ((p[offset + 0].toInt() and 0xFF) shl 8
				or (p[offset + 1].toInt() and 0xFF))
			return ret
		}
		
		/* encode 32 bits unsigned int (msb) */
		fun ikcp_encode32u(p: ByteArray, offset: Int, l: Long) {
			p[offset + 0] = (l shr 24).toByte()
			p[offset + 1] = (l shr 16).toByte()
			p[offset + 2] = (l shr 8).toByte()
			p[offset + 3] = (l shr 0).toByte()
		}
		
		/* decode 32 bits unsigned int (msb) */
		fun ikcp_decode32u(p: ByteArray, offset: Int): Long {
			val ret = (p[offset + 0].toLong() and 0xFFL) shl 24 or ((p[offset + 1].toLong() and 0xFFL) shl 16
				) or ((p[offset + 2].toLong() and 0xFFL) shl 8
				) or (p[offset + 3].toLong() and 0xFFL)
			return ret
		}
		
		fun <T> slice(list: ArrayList<T>, start: Int, stop: Int) {
			val size = list.size
			for (i in 0 until size) {
				if (i < stop - start) {
					list[i] = list[i + start]!!
				} else {
					list.removeAt(stop - start)
				}
			}
		}
		
		fun _imin_(a: Long, b: Long): Long {
			return if (a <= b) a else b
		}
		
		fun _imax_(a: Long, b: Long): Long {
			return if (a >= b) a else b
		}
		
		fun _ibound_(lower: Long, middle: Long, upper: Long): Long {
			return _imin_(_imax_(lower, middle), upper)
		}
		
		fun _itimediff(later: Long, earlier: Long): Int {
			return ((later - earlier).toInt())
		}
	}
}