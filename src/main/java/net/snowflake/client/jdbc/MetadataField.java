package net.snowflake.client.jdbc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetadataField {

    public MetadataField() {
    }

    private String name;
    private String typeName;
    private int type;
    private boolean nullable;
    @JsonProperty("byteLength")
    private int length;
    private int precision;
    private int scale;
    private boolean fixed;
    private SnowflakeType base;
    private MetadataField[] fields;

    public MetadataField(String name, String typeName, int type, boolean nullable, int length, int precision,
                         int scale, boolean fixed, SnowflakeType base, MetadataField[] fields) {
        this.name = name;
        this.typeName = typeName;
        this.type = type;
        this.nullable = nullable;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
        this.fixed = fixed;
        this.base = base;
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public SnowflakeType getBase() {
        return base;
    }

    public void setBase(SnowflakeType base) {
        this.base = base;
    }

    public MetadataField[] getFields() {
        return fields;
    }

    public void setFields(MetadataField[] fields) {
        this.fields = fields;
    }
}
