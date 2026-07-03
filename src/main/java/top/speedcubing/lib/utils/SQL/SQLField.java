package top.speedcubing.lib.utils.SQL;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import javax.sql.rowset.serial.SerialBlob;

public class SQLField {
    private final String columnName;
    private final Object object;

    public SQLField(String columnName, Object object) {
        this.columnName = columnName;
        this.object = object;
    }

    public String getColumnName() {
        return columnName;
    }

    public Object getObject() {
        return object;
    }

    public BigDecimal getBigDecimal() {
        return (BigDecimal) object;
    }

    public Blob getBlob() {
        try {
            if (object instanceof byte[]) {
                return new SerialBlob((byte[]) object);
            }
            return (Blob) object;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public Boolean getBoolean() {
        if(object instanceof Integer) {
            return (Integer) object == 1;
        }
        if (object instanceof String) {
            return "1".equals(object) || "true".equalsIgnoreCase((String) object);
        }
        return (Boolean) object;
    }

    public byte[] getBytes() {
        return (byte[]) object;
    }

    public Double getDouble() {
        return (Double) object;
    }

    public Float getFloat() {
        return (Float) object;
    }

    public Integer getInt() {
        if (object instanceof BigDecimal) {
            return ((BigDecimal) object).intValue();
        }
        if (object instanceof Long) {
            return ((Long) object).intValue();
        }
        if (object instanceof Boolean) {
            return ((Boolean) object) ? 1 : 0;
        }
        if (object instanceof Float) {
            return ((Float) object).intValue();
        }
        if (object instanceof Double) {
            return ((Double) object).intValue();
        }
        if (object instanceof String) {
            String s = (String) object;
            return s.isEmpty() ? 0 : Integer.parseInt(s);
        }
        return (Integer) object;
    }

    public Long getLong() {
        if (object instanceof BigDecimal) {
            return ((BigDecimal) object).longValue();
        }
        if(object instanceof Integer) {
            return ((Integer) object).longValue();
        }
        return (Long) object;
    }

    public String getString() {
        return object == null ? null : object.toString();
    }
}
