package org.umlg.sqlg.test.index;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.umlg.sqlg.test.BaseTest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Date: 2014/08/17
 * Time: 2:43 PM
 */
public class TestIndex extends BaseTest {

    @Test
    public void testIndexOnInteger() {
        this.sqlgGraph.createVertexLabeledIndex("Person", "name1", "dummy", "age", 1);
        this.sqlgGraph.tx().commit();
        for (int i = 0; i < 5000; i++) {
            this.sqlgGraph.addVertex(T.label, "Person", "name", "john" + i, "age", i);
        }
        this.sqlgGraph.tx().commit();
    }

    @Test
    public void testIndexOnVertex2() throws SQLException {
        this.sqlgGraph.createVertexLabeledIndex("Person", "name", "dummy");
        this.sqlgGraph.tx().commit();
        for (int i = 0; i < 5000; i++) {
            this.sqlgGraph.addVertex(T.label, "Person", "name", "john" + i);
        }
        this.sqlgGraph.tx().commit();
        Assert.assertEquals(1, this.sqlgGraph.traversal().V().has(T.label, "Person").has("name", "john50").count().next(), 0);
        Assert.assertEquals(1, this.sqlgGraph.traversal().V().has(T.label, "Person").has("name", P.eq("john50")).count().next().intValue());

        //Check if the index is being used
        Connection conn = this.sqlgGraph.tx().getConnection();
        Statement statement = conn.createStatement();
        if (this.sqlgGraph.getSqlDialect().getClass().getSimpleName().contains("Postgres")) {
            ResultSet rs = statement.executeQuery("explain analyze SELECT * FROM \"public\".\"V_Person\" a WHERE a.\"name\" = 'john50'");
            Assert.assertTrue(rs.next());
            String result = rs.getString(1);
            System.out.println(result);
            Assert.assertTrue(result.contains("Index Scan") || result.contains("Bitmap Heap Scan"));
            statement.close();
        }
        this.sqlgGraph.tx().rollback();
    }

    @Test
    public void testIndexOnVertex() throws SQLException {
        this.sqlgGraph.createVertexLabeledIndex("Person", "name1", "dummy", "name2", "dummy", "name3", "dummy");
        this.sqlgGraph.tx().commit();
        for (int i = 0; i < 5000; i++) {
            this.sqlgGraph.addVertex(T.label, "Person", "name1", "john" + i, "name2", "tom" + i, "name3", "piet" + i);
        }
        this.sqlgGraph.tx().commit();
        Assert.assertEquals(1, this.sqlgGraph.traversal().V().has(T.label, "Person").has("name1", "john50").count().next(), 0);
        Assert.assertEquals(1, this.sqlgGraph.traversal().V().has(T.label, "Person").has("name1", P.eq("john50")).count().next().intValue());

        //Check if the index is being used
        Connection conn = this.sqlgGraph.tx().getConnection();
        Statement statement = conn.createStatement();
        if (this.sqlgGraph.getSqlDialect().getClass().getSimpleName().contains("Postgres")) {
            ResultSet rs = statement.executeQuery("explain analyze SELECT * FROM \"public\".\"V_Person\" a WHERE a.\"name1\" = 'john50'");
            Assert.assertTrue(rs.next());
            String result = rs.getString(1);
            System.out.println(result);
            Assert.assertTrue(result.contains("Index Scan") || result.contains("Bitmap Heap Scan"));
            statement.close();
        }
        this.sqlgGraph.tx().rollback();
    }

    @Test
    public void testIndexOnVertex1() throws SQLException {
        //This is for postgres only
        Assume.assumeTrue(this.sqlgGraph.getSqlDialect().getClass().getSimpleName().contains("Postgres"));
        this.sqlgGraph.createVertexLabeledIndex("Person", "name1", "dummy", "name2", "dummy", "name3", "dummy");
        this.sqlgGraph.tx().commit();
        for (int i = 0; i < 5000; i++) {
            this.sqlgGraph.addVertex(T.label, "Person", "name1", "john" + i, "name2", "tom" + i, "name3", "piet" + i);
        }
        this.sqlgGraph.tx().commit();
        Assert.assertEquals(1, this.sqlgGraph.traversal().V().has(T.label, "Person").has("name1", "john50").count().next(), 0);
        Connection conn = this.sqlgGraph.getSqlgDataSource().get(this.sqlgGraph.getJdbcUrl()).getConnection();
        Statement statement = conn.createStatement();
        if (this.sqlgGraph.getSqlDialect().getClass().getSimpleName().contains("Postgres")) {
            ResultSet rs = statement.executeQuery("explain analyze SELECT * FROM \"public\".\"V_Person\" a WHERE a.\"name1\" = 'john50'");
            Assert.assertTrue(rs.next());
            String result = rs.getString(1);
            System.out.println(result);
            Assert.assertTrue(result.contains("Index Scan") || result.contains("Bitmap Heap Scan"));
            statement.close();
            conn.close();
        }
    }

//    @Test
//    public void testIndexOnEdge() throws SQLException {
//        this.sqlgGraph.createEdgeLabeledIndex("Schema0.edge", "name1", "dummy", "name2", "dummy", "name3", "dummy");
//        this.sqlgGraph.tx().commit();
//        Vertex previous = null;
//        for (int i = 0; i < 5; i++) {
//            for (int j = 0; j < 1000; j++) {
//                Vertex v = this.sqlgGraph.addVertex(T.label, "Schema" + i + ".Person", "name1", "n" + j, "name2", "n" + j);
//                if (previous != null) {
//                    previous.addEdge("edge", v, "name1", "n" + j, "name2", "n" + j);
//                }
//                previous = v;
//            }
//            this.sqlgGraph.tx().commit();
//        }
//        this.sqlgGraph.tx().commit();
//        Assert.assertEquals(1, this.sqlgGraph.traversal().E().has(T.label, "Schema0.edge").has("name1", "n500").count().next(), 0);
//        if (this.sqlgGraph.getSqlDialect().getClass().getSimpleName().contains("Postgres")) {
//            Connection conn = this.sqlgGraph.getSqlgDataSource().get(this.sqlgGraph.getJdbcUrl()).getConnection();
//            Statement statement = conn.createStatement();
//            ResultSet rs = statement.executeQuery("explain analyze SELECT * FROM \"Schema0\".\"E_edge\" a WHERE a.\"name1\" = 'n50'");
//            Assert.assertTrue(rs.next());
//            String result = rs.getString(1);
//            System.out.println(result);
//            Assert.assertTrue(result.contains("Index Scan") || result.contains("Bitmap Heap Scan"));
//            statement.close();
//            conn.close();
//        }
//    }

    @Test
    public void testIndexExist() {
        this.sqlgGraph.createVertexLabeledIndex("Person", "name", "a");
        this.sqlgGraph.tx().commit();
        this.sqlgGraph.createVertexLabeledIndex("Person", "name", "a");
        this.sqlgGraph.createVertexLabeledIndex("Person", "name", "a");
        this.sqlgGraph.tx().commit();
    }

    @Test
    public void testVertexSingleLabelUniqueConstraint() throws Exception {
        this.sqlgGraph.createVertexLabeledIndex("Person", "name", "a");
        this.sqlgGraph.createVertexUniqueConstraint("name", "Person");
        this.sqlgGraph.tx().commit();

        this.sqlgGraph.addVertex(T.label, "Person", "name", "Joe");
        this.sqlgGraph.tx().commit();

        try {
            this.sqlgGraph.addVertex(T.label, "Person", "name", "Joe");
            Assert.fail("Should not have been possible to add 2 people with the same name.");
        } catch (Exception e) {
            //good
            this.sqlgGraph.tx().rollback();
        }
    }

    @Test
    public void testVertexMultiLabelUniqueConstraint() throws Exception {
        this.sqlgGraph.createVertexLabeledIndex("Chocolate", "name", "a");
        this.sqlgGraph.createVertexLabeledIndex("Candy", "name", "a");
        this.sqlgGraph.createVertexLabeledIndex("Icecream", "name", "a");
        this.sqlgGraph.createVertexUniqueConstraint("name", "Chocolate", "Candy");

        this.sqlgGraph.addVertex(T.label, "Chocolate", "name", "Yummy");
        this.sqlgGraph.tx().commit();

        try {
            this.sqlgGraph.addVertex(T.label, "Candy", "name", "Yummy");
            Assert.fail("A chocolate and a candy should not have the same name.");
        } catch (Exception e) {
            //good
            this.sqlgGraph.tx().rollback();
        }

        this.sqlgGraph.addVertex(T.label, "Icecream", "name", "Yummy");
        this.sqlgGraph.tx().commit();
    }

    @Test
    public void testVertexMultipleConstraintsOnSingleProperty() throws Exception {
        this.sqlgGraph.createVertexLabeledIndex("Chocolate", "name", "a");
        this.sqlgGraph.createVertexLabeledIndex("Candy", "name", "a");
        this.sqlgGraph.createVertexLabeledIndex("Icecream", "name", "a");
        this.sqlgGraph.createVertexUniqueConstraint("name", "Chocolate");
        this.sqlgGraph.createVertexUniqueConstraint("name", "Candy");

        this.sqlgGraph.addVertex(T.label, "Chocolate", "name", "Yummy");
        this.sqlgGraph.addVertex(T.label, "Candy", "name", "Yummy");
        this.sqlgGraph.addVertex(T.label, "Icecream", "name", "Yummy");
        this.sqlgGraph.addVertex(T.label, "Icecream", "name", "Yummy");
        this.sqlgGraph.tx().commit();

        try {
            this.sqlgGraph.addVertex(T.label, "Chocolate", "name", "Yummy");
            Assert.fail("Two chocolates should not have the same name.");
        } catch (Exception e) {
            //good
            this.sqlgGraph.tx().rollback();
        }

        try {
            this.sqlgGraph.addVertex(T.label, "Candy", "name", "Yummy");
            Assert.fail("Two candies should not have the same name.");
        } catch (Exception e) {
            //good
            this.sqlgGraph.tx().rollback();
        }
    }

    @Test
    public void testVertexConstraintOnAnyLabel() throws Exception {
        this.sqlgGraph.createVertexLabeledIndex("Car", "name", "a");
        this.sqlgGraph.createVertexUniqueConstraint("name");
        this.sqlgGraph.createVertexLabeledIndex("Chocolate", "name", "a");
        this.sqlgGraph.createVertexLabeledIndex("Candy", "name", "a");

        this.sqlgGraph.addVertex(T.label, "Chocolate", "name", "Yummy");
        this.sqlgGraph.tx().commit();

        try {
            this.sqlgGraph.addVertex(T.label, "Candy", "name", "Yummy");
            Assert.fail("Should not be able to call a candy a pre-existing name.");
        } catch (Exception e) {
            //good
            this.sqlgGraph.tx().rollback();
        }

        try {
            this.sqlgGraph.addVertex(T.label, "Car", "name", "Yummy");
            Assert.fail("Should not be able to call a car a pre-existing name.");
        } catch (Exception e) {
            //good
            this.sqlgGraph.tx().rollback();
        }
    }
}
