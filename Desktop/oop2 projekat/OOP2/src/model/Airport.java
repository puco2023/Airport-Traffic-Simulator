package model;

public class Airport {
    private String name;
    private String code;
    private double x;
    private double y;
    private boolean visible = true;
    public Airport(String name, String code, double x, double y) {
        this.name = name;
        this.code = code;
        this.x = x;
        this.y = y;
    }
    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}