package host.kuro.onetwothree.datatype;

import host.kuro.onetwothree.NpcPlugin;

public class NpcInfo {
    public NpcPlugin.NPC_KIND kind;
    public String url;
    public String name;
    public String tag;
    public Float scale;

    public NpcInfo(NpcPlugin.NPC_KIND kind, String url, String name, String tag, Float scale) {
        this.kind = kind;
        this.url = url;
        this.name = name;
        this.tag = tag;
        this.scale = scale;
    }
}

