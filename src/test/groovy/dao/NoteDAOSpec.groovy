package dao

import io.vertx.core.Vertx
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import spock.lang.Shared
import spock.lang.Specification
import sqllite.SqlLiteClient

class NoteDAOSpec extends Specification {

    @Shared
    File dbFile = new File('db/dev.master.db')
    @Shared
    NoteDAO noteDAO

    def setupSpec() {
        FileUtils.deleteQuietly(this.dbFile)
        FileUtils.forceMkdir(new File('db'))

        def vertx = Vertx.vertx()
        def sqlLiteClient = new SqlLiteClient(vertx, "jdbc:sqlite:${this.dbFile.path}")
        this.noteDAO = new NoteDAO(sqlLiteClient)

        FileUtils.deleteQuietly()
        def script = IOUtils.toString(this.class.getResourceAsStream('/script.txt'), 'UTF-8')
        this.noteDAO.sqliteClient.execute(script).get()
    }

    def cleanupSpec() {
        FileUtils.deleteQuietly(this.dbFile)
    }

    def 'insert notes spec'() {
        given:
        def request = [title: 'test', plain_text: 'hihihi', raw_html: 'html']

        when:
        def result = this.noteDAO.insert(request).get()

        then:
        assert result.updated == 1
        assert result.keys

        def id = result.keys.getInteger(0)
        def note = this.noteDAO.findFirst([where: ['id=?': id]]).get()
        println note
        assert note.title == 'test'
        assert note.plain_text == 'hihihi'
        assert note.slug_url
        assert note.created_at
        assert note.status == 200
    }

    def 'updated notes spec:'() {
        given:
        def insertedResult = this.noteDAO.insert([title: 'title should update', plain_text: 'plain_text should update', raw_html: 'html']).get()
        def id = insertedResult.keys.first()
        def request = [where: ['id=?': id], update: [title: 'title updated', plain_text: 'plain_text updated', raw_html: 'html']]

        when:
        this.noteDAO.update(request).get()

        then:
        def note = this.noteDAO.findFirst([where: ['id=?': id]]).get()
        println note
        assert note.id == id
        assert note.updated_at
        assert note.title == 'title updated'
        assert note.plain_text == 'plain_text updated'
    }

    def 'delete note spec: '() {
        given:
        def insertedResult = this.noteDAO.insert([title: 'title should delete', plain_text: 'plain_text should delete', raw_html: 'html']).get()
        def id = insertedResult.keys.first()

        def request = [where: ['id=?': id]]
        when:
        this.noteDAO.delete(request).get()

        then:
        def note = this.noteDAO.findFirst([where: ['id=?': id]]).get()
        assert note == null
    }

    def 'find notes spec'() {
        given:
        def request = [select: ['*'], where: ['id = ?': 1]]
        when:
        def notes = this.noteDAO.finds(request).get()

        then:
        println notes

        1 == 1

    }

}
