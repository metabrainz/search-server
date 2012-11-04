package org.musicbrainz.search.servlet;

import java.util.HashMap;
import java.util.Map;

import org.musicbrainz.search.index.CDStubIndexField;

public class CDStubDismaxSearch extends AbstractDismaxSearchServer {

  @Override
  protected DismaxSearcher initDismaxSearcher() {
    Map<String, DismaxAlias.AliasField> fieldBoosts = new HashMap<String, DismaxAlias.AliasField>(2);
    fieldBoosts.put(CDStubIndexField.TITLE.getName(), new DismaxAlias.AliasField(true, 1.3f));
    fieldBoosts.put(CDStubIndexField.ARTIST.getName(), new DismaxAlias.AliasField(true, 1f));
    fieldBoosts.put(CDStubIndexField.COMMENT.getName(), new DismaxAlias.AliasField(false, 0.8f));
    fieldBoosts.put(CDStubIndexField.BARCODE.getName(), new DismaxAlias.AliasField(false, 0.8f));
    DismaxAlias dismaxAlias = new DismaxAlias();
    dismaxAlias.setFields(fieldBoosts);
    dismaxAlias.setTie(0.1f);
    return new DismaxSearcher(dismaxAlias);
  }

  /**
   * Standard Search
   *
   * @param searcher
   * @throws Exception
   */
  public CDStubDismaxSearch(AbstractSearchServer searchServer) throws Exception {
    super(searchServer);
  }

}
