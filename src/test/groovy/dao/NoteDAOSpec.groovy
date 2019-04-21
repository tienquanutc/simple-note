package dao

import io.vertx.core.Vertx
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.RandomUtils
import org.apache.commons.lang3.time.DateUtils
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

//        FileUtils.deleteQuietly()
//        def script = IOUtils.toString(this.class.getResourceAsStream('/script.txt'), 'UTF-8')
//        this.noteDAO.sqliteClient.execute(script).get()
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

    def 'insert dummy notes'() {
        given:
        def titles = ['Lorem ipsum dolor sit amet, consectetur adipiscing elit.'
                      , 'Donec suscipit nulla bibendum, hendrerit metus id, rutrum urna.'
                      , 'Cras luctus est quis tortor dignissim laoreet.'
                      , 'Donec sed dui mollis tortor suscipit commodo eget a augue.'
                      , 'Suspendisse at mi ac velit varius placerat.'
                      , 'Suspendisse viverra felis eu mi sollicitudin euismod.'
                      , 'In at libero a nisi placerat cursus sed vitae tortor.'
                      , 'Duis a est imperdiet, ullamcorper est sit amet, hendrerit tellus.'
                      , 'Aenean bibendum diam nec turpis ultricies, quis placerat eros rhoncus.'
                      , 'Curabitur euismod massa in nisi vulputate, id ultricies nibh laoreet.'
                      , 'Praesent sit amet leo sit amet mi faucibus luctus a in augue.'
                      , 'Praesent euismod nisl sodales mattis sollicitudin.'
                      , 'Donec euismod tortor ut leo commodo, vitae eleifend libero iaculis.'
                      , 'Mauris id nisl tincidunt nibh rhoncus faucibus vel at enim.'
                      , 'Fusce tincidunt mi vitae augue scelerisque, euismod vulputate diam ultrices.'
                      , 'Ut sit amet erat facilisis nulla efficitur vulputate sit amet at metus.'
                      , 'Pellentesque eu nisl sit amet odio semper ultricies in vitae erat.'
                      , 'Proin ut ante id ante lacinia vehicula id non diam.'
                      , 'Vestibulum vitae enim eu sem finibus tincidunt.'
                      , 'Mauris dictum erat ut risus eleifend, nec lobortis nulla dignissim.'
                      , 'Donec fringilla orci non venenatis volutpat.'
                      , 'Sed porttitor ante vulputate interdum tincidunt.'
                      , 'Nam hendrerit ipsum vel diam posuere pretium.'
                      , 'Vestibulum tempor justo rutrum ante congue posuere.'
                      , 'Quisque placerat nunc eu magna fringilla, commodo condimentum justo dignissim.'
                      , 'Suspendisse gravida leo et efficitur ultricies.'
                      , 'Fusce luctus metus sed tellus tincidunt, et pharetra tellus maximus', 'Mauris id nisl tincidunt nibh rhoncus faucibus vel at enim.'
                      , 'Fusce tincidunt mi vitae augue scelerisque, euismod vulputate diam ultrices.'
                      , 'Ut sit amet erat facilisis nulla efficitur vulputate sit amet at metus.'
                      , 'Pellentesque eu nisl sit amet odio semper ultricies in vitae erat.'
                      , 'Proin ut ante id ante lacinia vehicula id non diam.'
                      , 'Vestibulum vitae enim eu sem finibus tincidunt.'
                      , 'Mauris dictum erat ut risus eleifend, nec lobortis nulla dignissim.'
                      , 'Donec fringilla orci non venenatis volutpat.'
                      , 'Sed porttitor ante vulputate interdum tincidunt.'
                      , 'Nam hendrerit ipsum vel diam posuere pretium.'
                      , 'Vestibulum tempor justo rutrum ante congue posuere.'
                      , 'Quisque placerat nunc eu magna fringilla, commodo condimentum justo dignissim.'
                      , 'Suspendisse gravida leo et efficitur ultricies.'
                      , 'Fusce luctus metus sed tellus tincidunt, et pharetra tellus maximus']

        def now = new Date()
        when:
        titles.each {
            def randomDate = DateUtils.addMinutes(now, RandomUtils.nextInt(0, 60) - 60) //-60->0
            this.noteDAO.insert([title: it, plain_text: it, private: 0, raw_html: it, created_at: randomDate]).get()
        }
        then:
        1 == 1
    }

}
