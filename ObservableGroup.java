import android.databinding.ObservableList;
import android.support.annotation.NonNull;

import com.genius.groupie.Group;
import com.genius.groupie.Section;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Function;

public class ObservableGroup<T> extends Section {
    private final Function<T, Group> groupFactory;

    public ObservableGroup(@NonNull final ObservableList<T> observableList, final Function<T, Group> groupFactory) {
        this.groupFactory = groupFactory;
        observableList.addOnListChangedCallback(listChangedCallback);
        listChangedCallback.onItemRangeInserted(observableList, 0, observableList.size());
    }

    // OnListChangedCallback

    private final ObservableList.OnListChangedCallback<ObservableList<T>> listChangedCallback = new ObservableList.OnListChangedCallback<ObservableList<T>>() {
        @Override
        public void onChanged(final ObservableList<T> sender) {

        }

        @Override
        public void onItemRangeChanged(final ObservableList<T> sender, final int positionStart, final int itemCount) {

        }

        @Override
        public void onItemRangeInserted(final ObservableList<T> sender, final int positionStart, final int itemCount) {
            final List<T> inserts = sender.subList(positionStart, positionStart + itemCount);
            for (int i = 0; i < inserts.size(); i++) {
                final T item = inserts.get(i);
                final Group group;
                try {
                    group = groupFactory.apply(item);
                    add(positionStart + i, group);
                } catch (Exception e) {
                }
            }
        }

        @Override
        public void onItemRangeMoved(final ObservableList<T> sender, final int fromPosition, final int toPosition, final int itemCount) {

        }

        @Override
        public void onItemRangeRemoved(final ObservableList<T> sender, final int positionStart, final int itemCount) {
            final List<Group> groupsToRemove = new ArrayList<>(itemCount);
            for (int i = positionStart; i < positionStart + itemCount; i++) {
                groupsToRemove.add(getGroup(i));
            }
            removeAll(groupsToRemove);
        }
    };
}
