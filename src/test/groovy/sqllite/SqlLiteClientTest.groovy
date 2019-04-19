package sqllite

import io.vertx.core.Vertx
import spock.lang.Shared
import spock.lang.Specification

import java.sql.ResultSet

class SqlLiteClientTest extends Specification {

    @Shared
    SqlLiteClient sqlLiteClient

    def setupSpec() {
        def vertx = Vertx.vertx()
        this.sqlLiteClient = new SqlLiteClient(vertx, "jdbc:sqlite:db/dev.master.db")
    }

    def setup() {
        this.sqlLiteClient.execute("CREATE TABLE User (ID Int, Name Varchar)").get()
    }

    def cleanup() {
        this.sqlLiteClient.execute("DROP TABLE IF EXISTS User").get()
    }

    def "insert db"() {
        given:
        String sql = "INSERT INTO User VALUES (1, 'quannt')"

        when:
        def updateResult = this.sqlLiteClient.update(sql).get()
        io.vertx.ext.sql.ResultSet resultSet = this.sqlLiteClient.query("SELECT * FROM User WHERE ID = 1").get()

        then:
        def row = resultSet.getRows().first()
        assert updateResult.updated > 0
        assert row != null
        assert row.getInteger('ID') == 1
        assert row.getString('Name') == 'quannt'

    }
}
