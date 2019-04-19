package hanlder

import app.AppConfig
import com.spotify.futures.CompletableFutures
import dao.NoteDAO
import io.vertx.core.MultiMap
import io.vertx.ext.web.RoutingContext
import vertx.VertxViewHandler

class NOTE extends VertxViewHandler<AppConfig> {

    NoteDAO noteDAO

    NOTE(AppConfig config) {
        super(config, '/note.peb')
        this.noteDAO = config.noteDAO
    }

    @Override
    void handle(RoutingContext ctx) {
        String notePathParam = ctx.pathParam('note_slug_url')
        String noteBaseSlugUrl = ctx.request().getParam('base')

        String noteSlugUrl = notePathParam ? URLDecoder.decode(notePathParam, 'UTF-8') : noteBaseSlugUrl

        def recentNotesFuture = this.noteDAO.queryNotes([limit: 8])
        if (!noteSlugUrl) {
            recentNotesFuture.thenApply { recentNotes ->
                ctx.put('recentNotes', recentNotes)
            }
            this.renderView(ctx)
            return
        }

        def noteFuture = this.noteDAO.findBySlug(noteSlugUrl)

        CompletableFutures.combine(noteFuture, recentNotesFuture) { note, recentNotes ->
            if (!note) {
                ctx.next()
                return
            }

            //flash message
            def message = ctx.session().get('message')
            if (message) ctx.session().remove('message')

            ctx.put('message', message)
            ctx.put('note', note)
            ctx.put('recentNotes', recentNotes)
            ctx.put('readonly', ctx.pathParam('note_slug_url') != null)

            this.renderView(ctx)
        }.exceptionally(ctx.&fail)
    }

    void handlePostNote(RoutingContext ctx) {
        MultiMap formAttributes = ctx.request().formAttributes()

        String title = formAttributes.get('title')
        Integer _private = formAttributes.get('private') == null ? 0 : 1
        String plainText = formAttributes.get('plain_text')
        String rawHtml = formAttributes.get('raw_html')

        if (!title || !plainText || !rawHtml) {
            this.noteDAO.queryNotes([limit: 8]).thenApply { recentNotes ->
                ctx.put('recentNotes', recentNotes)
                ctx.put('message', [type: 'warning', title: "Message", body: 'Please enter your note content you want to save. Just do it as well.'])
                this.renderView(ctx)
            }.exceptionally(ctx.&fail)
            return
        }

        this.noteDAO.insert([title: title, private: _private, plain_text: plainText, raw_html: rawHtml]).thenCompose { result ->
            Integer id = result.keys.getInteger(0)
            this.noteDAO.findFirst([where: ['id=?': id]])
        }.thenAccept { note ->
            String redirectLocation = "/notes/${URLEncoder.encode(note.slug_url, 'UTF-8')}/"
            ctx.session().put('message', [type: 'success', title: 'Success', body: 'You have saved your note successful. You can see Share URL below. Just copy it and share your note to everyone right now.'])
            ctx.response()
                    .setStatusCode(302)
                    .putHeader('location', redirectLocation)
                    .end()
        }.exceptionally(ctx.&fail)
    }

    void handleAllNotes(RoutingContext ctx) {
        def pageSize = 12
        def currentPageIndex = ctx.request().getParam('page') as Integer ?: 1
        def pagingQuery = ctx.request().query()?.replaceAll("page=\\d+&?", '')
        def path = ctx.request().path()
        if (!path.endsWith('/')) path += '/'

        def skip = pageSize * (currentPageIndex - 1)
        def notesFuture = this.noteDAO.queryNotes([skip: skip, limit: pageSize])
        def countNotesFuture = this.noteDAO.countNotes()

        CompletableFutures.combine(notesFuture, countNotesFuture) { notes, count ->
            def meta = [
                    currentPageIndex: currentPageIndex,
                    totalPages      : Math.ceil(count / pageSize as Double),
                    pageSize        : pageSize
            ]

            ctx.put('notes', notes)
            ctx.put('pagingQuery', pagingQuery)
            ctx.put('meta', meta)
            ctx.put('path', path)

            this.renderView(ctx, '/all_notes.peb')
        }.exceptionally { e -> e.printStackTrace(); ctx.fail(e); }
    }

}
