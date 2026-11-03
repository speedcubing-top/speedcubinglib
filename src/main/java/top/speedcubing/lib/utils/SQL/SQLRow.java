package top.speedcubing.lib.utils.SQL;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;

public class SQLRow implements Iterable<SQLField> {

    private final List<SQLField> fields = new ArrayList<>();

    public SQLRow(ResultSet result) {
        try {
            ResultSetMetaData metaData = result.getMetaData();
            int l = metaData.getColumnCount();
            for (int i = 1; i <= l; i++) {
                fields.add(new SQLField(metaData.getColumnName(i), result.getObject(i)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Builds a row from already-read fields (e.g. a cached copy of a JDBC row). */
    public SQLRow(List<SQLField> fields) {
        this.fields.addAll(fields);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return fields.size();
    }

    public SQLField get(String columnName) {
        for (SQLField f : fields) {
            if (f.getColumnName().equals(columnName)) {
                return f;
            }
        }
        return null;
    }

    public SQLField get(int i) {
        if(i < 0 || i >= fields.size()) {
            return null;
        }
        return fields.get(i);
    }

    public Object getObject(String columnName) {
        return get(columnName).getObject();
    }

    public Object getObject(int i) {
        return get(i).getObject();
    }

    public String getColumnName(int i) {
        return get(i).getColumnName();
    }

    public BigDecimal getBigDecimal(String s) {
        return get(s).getBigDecimal();
    }

    public BigDecimal getBigDecimal(int i) {
        return get(i).getBigDecimal();
    }

    public Blob getBlob(String s) {
        return get(s).getBlob();
    }

    public Blob getBlob(int i) {
        return get(i).getBlob();
    }

    public Boolean getBoolean(String s) {
        return get(s).getBoolean();
    }

    public Boolean getBoolean(int i) {
        return get(i).getBoolean();
    }

    public byte[] getBytes(String s) {
        return get(s).getBytes();
    }

    public byte[] getBytes(int i) {
        return get(i).getBytes();
    }

    public Double getDouble(String s) {
        return get(s).getDouble();
    }

    public Double getDouble(int i) {
        return get(i).getDouble();
    }

    public Float getFloat(String s) {
        return get(s).getFloat();
    }

    public Float getFloat(int i) {
        return get(i).getFloat();
    }

    public Integer getInt(String s) {
        return get(s).getInt();
    }

    public Integer getInt(int i) {
        return get(i).getInt();
    }

    public Long getLong(String s) {
        return get(s).getLong();
    }

    public Long getLong(int i) {
        return get(i).getLong();
    }

    public String getString(String s) {
        return get(s).getString();
    }

    public String getString(int i) {
        return get(i).getString();
    }

    @NotNull
    @Override
    public Iterator<SQLField> iterator() {
        return fields.iterator();
    }

    @Override
    public void forEach(Consumer<? super SQLField> action) {
        fields.forEach(action);
    }
}
