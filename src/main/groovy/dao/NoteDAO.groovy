package dao

import io.vertx.ext.sql.UpdateResult
import org.apache.commons.lang3.RandomStringUtils
import sqllite.SqlLiteClient
import utils.UrlSlugger

import java.util.concurrent.CompletableFuture

class NoteDAO extends BaseDAO {

    NoteDAO(SqlLiteClient sqlLiteClient) {
        super(sqlLiteClient, 'notes')
    }

    @Override
    CompletableFuture<UpdateResult> insert(Map request) {
        if (request.title && !request.slug_url) {
            request.slug_url = UrlSlugger.slug(request.title as String)
            //make slug unique
            request.slug_url += '-' + RandomStringUtils.random(6, true, true)
        }

        if (!request.created_at) {
            request.created_at = new Date()
        }
        return super.insert(request)
    }

    @Override
    CompletableFuture<UpdateResult> update(Map request) {
        Map update = request.update as Map
        if (update && !update.updated_at) {
            update.updated_at = new Date()
        }

        return super.update(request)
    }

    CompletableFuture<Map> findBySlug(String slugUrl) {
        return this.findFirst([where: ['slug_url=?': slugUrl]])
    }

    CompletableFuture<List<Map>> queryNotes(Map params) {
        Integer limit = params.limit as Integer ?: 3
        Integer skip = params.skip as Integer ?: 0
        return this.finds([select: ['title', 'slug_url', 'created_at'], where: ['private=?': 0], orderBy: [created_at: 'desc'], limit: limit, skip: skip])
    }

    CompletableFuture<Integer> countNotes() {
        return this.findFirst([select: ['count(*) as count'], where:['private = ?': 0]]).thenApply { it.count }
    }
}
