package ng.codehaven.bambam.models;

import io.realm.Realm;
import io.realm.RealmMigration;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

/**
 * Created by Thompson on 8/28/2015.
 */
public class Migration implements RealmMigration {
    @Override
    public long execute(Realm realm, long version) {
        if (version == 0){
            Table trackTable = realm.getTable(Track.class);

            trackTable.addColumn(ColumnType.BOOLEAN, "isLiked");
            trackTable.addColumn(ColumnType.STRING, "commentsArray");

        }
        return version;
    }
}
