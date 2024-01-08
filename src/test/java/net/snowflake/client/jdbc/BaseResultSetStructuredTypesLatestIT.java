package net.snowflake.client.jdbc;

import net.snowflake.client.category.TestCategoryResultSet;
import net.snowflake.client.core.structs.SnowflakeObjectTypeFactories;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(TestCategoryResultSet.class)
public abstract class BaseResultSetStructuredTypesLatestIT {
  private final String queryResultFormat;

  protected BaseResultSetStructuredTypesLatestIT(String queryResultFormat) {
    this.queryResultFormat = queryResultFormat;
  }

  public Connection init() throws SQLException {
    Connection conn = BaseJDBCTest.getConnection(BaseJDBCTest.DONT_INJECT_SOCKET_TIMEOUT);
    Statement stmt = conn.createStatement();
    stmt.execute("alter session set jdbc_query_result_format = '" + queryResultFormat + "'");
    stmt.close();
    return conn;
  }

  public static class SimpleClass implements SQLData {

    private String string;

    public SimpleClass(String string) {
      this.string = string;
    }
    public SimpleClass() {}

    @Override
    public String getSQLTypeName() throws SQLException {
      return null;
    }

    @Override
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
      string = stream.readString();
    }

    @Override
    public void writeSQL(SQLOutput stream) throws SQLException {
      stream.writeString( string);
    }
  }

  public static class AllTypesClass implements SQLData {

    private String string;
    private Byte b;
    private Short s;
    private Integer i;
    private Long l;
    private Float f;
    private Double d;
    private Boolean bool;
    private SimpleClass simpleClass;

    @Override
    public String getSQLTypeName() throws SQLException {
      return null;
    }

    @Override
    public void readSQL(SQLInput sqlInput, String typeName) throws SQLException {
      string = sqlInput.readString();
      b = sqlInput.readByte();
      s = sqlInput.readShort();
      i = sqlInput.readInt();
      l = sqlInput.readLong();
      f = sqlInput.readFloat();
      d = sqlInput.readDouble();
      bool = sqlInput.readBoolean();
      simpleClass = sqlInput.readObject(SimpleClass.class);
    }

    @Override
    public void writeSQL(SQLOutput stream) throws SQLException {
    }
  }

  @Test
  public void testMapStructToObjectWithFactory() throws SQLException {
    testMapJson(true);
  }

  @Test
  public void testMapStructToObjectWithReflection() throws SQLException {
    testMapJson(false);
  }

  private void testMapJson(boolean registerFactory) throws SQLException {
    if (registerFactory) {
      SnowflakeObjectTypeFactories.register(SimpleClass.class, SimpleClass::new);
    } else {
      SnowflakeObjectTypeFactories.unregister(SimpleClass.class);
    }
    Connection connection = init();
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select {'string':'a'}::OBJECT(string VARCHAR)");
    resultSet.next();
    SimpleClass object = resultSet.getObject(1, SimpleClass.class);
    assertEquals("a", object.string);
    statement.close();
    connection.close();
  }

  @Test
  public void testMapAllTypesOfFields() throws SQLException {
    SnowflakeObjectTypeFactories.register(AllTypesClass.class, AllTypesClass::new);
    Connection connection = init();
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select {" +
        "'string': 'a', " +
        "'b': 1, " +
        "'s': 2, " +
        "'i': 3, " +
        "'l': 4, " +
        "'f': 1.1, " +
        "'d': 2.2, " +
        "'bool': true, " +
        "'simpleClass': {'string': 'b'}" +
        "}::OBJECT(" +
        "string VARCHAR, " +
        "b TINYINT, " +
        "s SMALLINT, " +
        "i INTEGER, " +
        "l BIGINT, " +
        "f FLOAT, " +
        "d DOUBLE, " +
        "bool BOOLEAN, " +
        "simpleClass OBJECT(string VARCHAR)" +
        ")");
    resultSet.next();
    AllTypesClass object = resultSet.getObject(1, AllTypesClass.class);
    assertEquals("a", object.string);
    assertEquals(1, (long) object.b);
    assertEquals(2, (long) object.s);
    assertEquals(3, (long) object.i);
    assertEquals(4, (long) object.l);
    assertEquals(1.1, (double) object.f, 0.01);
    assertEquals(2.2, (double) object.d, 0.01);
    assertTrue(object.bool);
    assertEquals("b", object.simpleClass.string);
    statement.close();
    connection.close();
  }

  @Test
  public void testMapStructsFromChunks() throws SQLException {
    Connection connection = init();
    Statement statement = connection.createStatement();
    ResultSet resultSet =
        statement.executeQuery(
            "select {'string':'a'}::OBJECT(string VARCHAR) FROM TABLE(GENERATOR(ROWCOUNT=>30000))");
    int i = 0;
    while (resultSet.next()) {
      SimpleClass object = resultSet.getObject(1, SimpleClass.class);
      assertEquals("a", object.string);
    }
    statement.close();
    connection.close();
  }


  @Test
  public void testMapArray() throws SQLException {
    testMapArrayWithClientMapping();
    testMapArrayByCustomInterface();
    testMapArrayByGetObject();
  }

  private void testMapArrayWithClientMapping() throws SQLException {
    SnowflakeObjectTypeFactories.register(SimpleClass.class, SimpleClass::new);
    Connection connection = init();
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select [{'string':'aaa'},{'string': 'bbb'}]::ARRAY(OBJECT(string varchar))");
    resultSet.next();
    Array array = resultSet.getArray(1);
    List<SQLInput> inputs = ((List<SQLInput>)array.getArray());
    SimpleClass[] objects = new SimpleClass[inputs.size()];
    AtomicInteger counter = new AtomicInteger(0);
    inputs.stream().forEach(sqlInput ->{
      SimpleClass sc = new SimpleClass();
      try {
        sc.readSQL(sqlInput, null);
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
      objects[counter.getAndIncrement()] = sc;
    });

    assertEquals(objects[0].string, "aaa");
    assertEquals(objects[1].string, "bbb");

    statement.close();
    connection.close();
  }
  private void testMapArrayByCustomInterface() throws SQLException {
    SnowflakeObjectTypeFactories.register(SimpleClass.class, SimpleClass::new);
    Connection connection = init();
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select [{'string':'aaa'},{'string': 'bbb'}]::ARRAY(OBJECT(string varchar))");
    resultSet.next();
    SimpleClass[] objects = resultSet.unwrap(SnowflakeBaseResultSet.class).getArray(1,  SimpleClass.class);
    assertEquals(objects[0].string, "aaa");
    assertEquals(objects[1].string, "bbb");

    statement.close();
    connection.close();
  }

  private void testMapArrayByGetObject() throws SQLException {
    SnowflakeObjectTypeFactories.register(SimpleClass.class, SimpleClass::new);
    Connection connection = init();
    Statement statement = connection.createStatement();
    ResultSet resultSet = statement.executeQuery("select [{'string':'aaa'},{'string': 'bbb'}]::ARRAY(OBJECT(string varchar))");
    resultSet.next();
    SimpleClass[] objects = resultSet.getObject(1, SimpleClass[].class);

    assertEquals(objects[0].string, "aaa");
    assertEquals(objects[1].string, "bbb");

    statement.close();
    connection.close();
  }


}
