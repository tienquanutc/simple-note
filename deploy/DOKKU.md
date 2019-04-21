### Redeploy Dokku

```
git add . && git commit -m "redeploy" && git push dokku master
```

### Init Dokku
```
git init . && git remote add dokku dokku@quannt:notepad-site
git pull dokku master
```

### Create App
```
dokku apps:create notepad-site
dokku domains:add notepad-site note.quannt.me www.note.quannt.me
dokku redirect:set notepad-site www.note.quannt.me note.quannt.me 301
dokku storage:mount notepad-site /home/db:/db
```