package host.kuro.onetwothree.protocol;

import cn.nukkit.network.protocol.*;

public class SetDisplayObjectivePacket extends DataPacket{
	
	public String displaySlot;
	public String objectiveName;
	public String displayName;
	public String criteriaName;
	public int sortOrder;

	@Override
	public void decode() {
		displaySlot = this.getString();
		objectiveName = this.getString();
		displayName = this.getString();
		criteriaName = this.getString();
		sortOrder = this.getVarInt();
		
	}

	@Override
	public void encode() {
		this.reset();
		this.putString(displaySlot);
		this.putString(objectiveName);
		this.putString(displayName);
		this.putString(criteriaName);
		this.putVarInt(sortOrder);
	}

	@Override
	public byte pid() {
		return 0x6b;
	}

}
