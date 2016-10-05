package org.umlg.sqlg.test.tp3.structure;

import org.apache.tinkerpop.gremlin.GraphProviderClass;
import org.apache.tinkerpop.gremlin.structure.StructurePerformanceSuite;
import org.junit.runner.RunWith;
import org.umlg.sqlg.structure.SqlgGraph;
import org.umlg.sqlg.test.tp3.SqlgPostgresProvider;

/**
 * Executes the Gremlin Structure Performance Test Suite using SqlgGraph on postgresql.
 */
@RunWith(StructurePerformanceSuite.class)
@GraphProviderClass(provider = SqlgPostgresProvider.class, graph = SqlgGraph.class)
public class SqlgPostgresGroovyStructurePerformanceTest {

}