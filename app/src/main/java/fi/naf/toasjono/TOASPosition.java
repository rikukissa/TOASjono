package fi.naf.toasjono;

/**
 * Created by pyryr on 17.9.2017.
 */

class TOASPosition {
    private Integer key;
    private String house;
    private Integer position;

    TOASPosition(Integer key, String house, Integer position) {
        this.key = key;
        this.house = house;
        this.position = position;
    }

    public int getPos() {
        return this.position;
    }

    @Override
    public String toString() {
        return this.key + " " + this.house;
    }
}
