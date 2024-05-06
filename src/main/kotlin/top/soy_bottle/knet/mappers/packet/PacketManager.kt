package top.soy_bottle.knet.mappers.packet

import top.soy_bottle.knet.logger
import top.soy_bottle.knet.utils.readByteArray
import top.soy_bottle.knet.utils.readInt
import top.soy_bottle.knet.utils.writeByteArray
import top.soy_bottle.knet.utils.writeInt
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream


abstract class PacketManager {
	val recivePacketHandlers: MutableMap<Class<out Packet>, (Packet) -> Unit> = mutableMapOf()
	val recivePacketClasses: MutableList<Class<out Packet>> = mutableListOf()
	val revicePacketCreator: MutableList<(InputStream) -> Packet> = mutableListOf()
	
	val sendIds: MutableMap<Class<out Packet>, Int> = linkedMapOf()
	
	fun idByClass(clazz: Class<Packet>) = sendIds[clazz]
	
	fun classById(id: Int) = recivePacketClasses[id]
	
	protected inline fun <reified T : Packet> registerRecivePacket(noinline create: (InputStream) -> T) {
		recivePacketClasses.add(T::class.java)
		revicePacketCreator.add(create)
	}
	
	inline fun <reified T : Packet> registerRecivePacket(
		noinline create: (InputStream) -> T, crossinline handler: (T) -> Unit,
	) {
		recivePacketClasses.add(T::class.java)
		revicePacketCreator.add(create)
		recivePacketHandlers[T::class.java] = { it: Packet -> handler(it as T) }
	}
	
	inline fun <reified T : Packet> registerSendPacket() {
		sendIds[T::class.java] = sendIds.size
	}
	
	fun readPacketAndHandle(i: InputStream): Packet {
		val packet = readPacket(i)
//		logger.info("read packet:${packet.javaClass}")
		
		recivePacketHandlers[packet.javaClass]!!(packet)
		return packet
	}
	
	fun readPacket(i: InputStream): Packet {
//		/*test only*/
//		val bytes = i.readByteArray()
//		return bytes.inputStream().objectStream().readObject() as Packet


		val id = i.readInt()
//		val bytes = i.readByteArray()
		logger.info("{C<->S} read type->${recivePacketClasses[id].simpleName}")

//		return revicePacketCreator[id](bytes.inputStream())
		return revicePacketCreator[id](i)
	}
	
	fun <O : OutputStream> writePacket(out: O, packet: Packet) = out.apply {
		writeInt(sendIds[packet.javaClass]!!)
//		println("Write Pakcet ID:${sendIds[packet.javaClass]!!}")
		packet.write(this)
//		/*test object only*/
//		val bytes = ByteArrayOutputStream()
//		bytes.objectStream().writeObject(packet)
//		writeByteArray(bytes.toByteArray())
		
//		packet.write(this)
		
		flush()
		logger.info("{C<->S} write packet->${packet.javaClass.simpleName}")
		
	}
	
}