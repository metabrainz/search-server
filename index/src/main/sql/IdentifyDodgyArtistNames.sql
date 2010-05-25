select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name like '*%'
union
select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name like '!%'
union
select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name like '[%'
union
select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name like '\'%'
union
select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name like '\"%'
union
select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name like ']%'
union
select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name like '.%'
union
select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name like ';%'
union
select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name like ',%'
union
select 'http://musicbrainz.org/artist/' ||artist.gid ||'.html' ,artist_name.name from artist inner join artist_name on artist.name=artist_name.id
where artist_name.name = '%'
ORDER BY name