package host.kuro.onetwothree.item;

import host.kuro.onetwothree.BasePlugin;
import host.kuro.onetwothree.database.DatabaseManager;

public class ItemPrice {
    public int index;
    public int id;
    public String name;
    public int price;

    public ItemPrice(int index, int id, String name, int price) {
        this.index = index;
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
