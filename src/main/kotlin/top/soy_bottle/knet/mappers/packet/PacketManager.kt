package top.soy_bottle.knet.mappers.packet

import top.soy_bottle.knet.utils.readByteArray
import top.soy_bottle.knet.utils.readVarInt
import top.soy_bottle.knet.utils.writeVarInt
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream


abstract class PacketManager {
	protected val recivePacketClasses: MutableList<Class<out Packet>> = mutableListOf()
	protected val revicePacketCreator: MutableList<(InputStream) -> Packet> = mutableListOf()
	
	protected val sendIds: MutableMap<Class<out Packet>, Int> = linkedMapOf()
	
	fun idByClass(clazz: Class<Packet>) = sendIds[clazz]
	
	fun classById(id: Int) = recivePacketClasses[id]
	
	protected inline fun <reified T : Packet> registerRecivePacket(noinline create: (InputStream) -> T) {
		recivePacketClasses.add(T::class.java)
		revicePacketCreator.add(create)
	}
	
	protected inline fun <reified T : Packet> registerSendPacket() {
		sendIds[T::class.java] = sendIds.size
	}
	
	fun readPacket(i: InputStream): Packet {
		val id = i.readVarInt()
		val bytes = i.readByteArray()
		
		return revicePacketCreator[id](bytes.inputStream())
	}
	
	fun <O : OutputStream> writePacket(out: O, packet: Packet): O {
		out.writeVarInt(sendIds[packet.javaClass]!!)
		val bytes = ByteArrayOutputStream()
		packet.write(bytes)
		out.writeVarInt(bytes.size())
		out.write(bytes.toByteArray())
		out.flush()
		
		return out
	}
	
}