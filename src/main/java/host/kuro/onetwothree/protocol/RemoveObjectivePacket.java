package host.kuro.onetwothree.protocol;

import cn.nukkit.network.protocol.DataPacket;

public class RemoveObjectivePacket extends DataPacket {

	public String objectiveName;
	
	@Override
	public void decode() {
		this.objectiveName = this.getString();
		
	}

	@Override
	public void encode() {
		this.reset();
		this.putString(this.objectiveName);
	}

	@Override
	public byte pid() {
		return 0x6a;
	}

}
