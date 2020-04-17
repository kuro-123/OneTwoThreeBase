package host.kuro.onetwothree.protocol;

import java.util.ArrayList;
import java.util.List;

import cn.nukkit.network.protocol.*;

public class SetScorePacket extends DataPacket{
	
	public byte type;
	public List<ScorePacketInfo> entries;

	@Override
	public void decode() {
		type = (byte) this.getByte();
		entries = this.getScorePacketInfos();
	}

	@Override
	public void encode() {
		this.reset();
		this.putByte(type);
		this.putScorePacketInfos(entries);
	}

	@Override
	public byte pid() {
		return 0x6c;
	}
	
	public void putScorePacketInfos(List<ScorePacketInfo> info) {
		this.putUnsignedVarInt(info.size());
		for(ScorePacketInfo entry : info) {
			this.putVarLong(entry.scoreboardId);
			this.putString(entry.objectiveName);
			this.putLInt(entry.score);
			this.putByte(entry.addType);
			switch(entry.addType) {
			case 1:
			case 2:
				this.putEntityUniqueId(entry.entityId);
				break;
			case 3:
				this.putString(entry.fakePlayer);
			}
		}
	}
	
	public List<ScorePacketInfo> getScorePacketInfos(){
		List<ScorePacketInfo> info = new ArrayList<ScorePacketInfo>();
		long length = this.getUnsignedVarInt();
		for(int i = 0; i <= (int) length; i++) {
			ScorePacketInfo entry = new ScorePacketInfo();
			entry.scoreboardId = this.getVarLong();
			entry.objectiveName = this.getString();
			entry.score = this.getLInt();
			if(this.type == 0) {
				entry.addType = (byte) this.getByte();
				switch(entry.addType) {
				case 1:
				case 2:
					entry.entityId = this.getEntityUniqueId();
					break;
				case 3:
					entry.fakePlayer = this.getString();
				}
			}
			info.add(entry);
		}
		return info;
	}

}
