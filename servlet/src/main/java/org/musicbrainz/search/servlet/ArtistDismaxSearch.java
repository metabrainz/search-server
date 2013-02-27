package org.musicbrainz.search.servlet;

import java.util.HashMap;
import java.util.Map;

import org.musicbrainz.search.index.ArtistIndexField;

public class ArtistDismaxSearch extends AbstractDismaxSearchServer {

  @Override
  protected DismaxSearcher initDismaxSearcher() {
    Map<String, DismaxAlias.AliasField> fieldBoosts = new HashMap<String, DismaxAlias.AliasField>(3);
    fieldBoosts.put(ArtistIndexField.ARTIST_ACCENT.getName(), new DismaxAlias.AliasField(false, 1.4f));
    fieldBoosts.put(ArtistIndexField.ARTIST.getName(), new DismaxAlias.AliasField(true, 1.2f));
    fieldBoosts.put(ArtistIndexField.SORTNAME.getName(), new DismaxAlias.AliasField(true, 1.1f));
    fieldBoosts.put(ArtistIndexField.ALIAS.getName(), new DismaxAlias.AliasField(true, 0.9f));
    DismaxAlias dismaxAlias = new DismaxAlias();
    dismaxAlias.setFields(fieldBoosts);
    dismaxAlias.setTie(0.1f);
    return new DismaxSearcher(dismaxAlias);
  }

  /**
   * Standard Search
   *
   * @param searchServer
   * @throws Exception
   */
  public ArtistDismaxSearch(AbstractSearchServer searchServer) throws Exception {
    super(searchServer);
  }

}
