import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.functions.BiPredicate;
import io.reactivex.functions.Function;

public class DiffListMapper<T, R> {
    private interface TriConsumer<T1, T2, T3> {
        void accept(T1 t1, T2 t2, T3 t3);
    }

    private final TriConsumer<List<T>, List<T>, List<R>> theFunction;

    public DiffListMapper(final BiPredicate<T, T> areSame, final Function<T, R> mapper) {
        theFunction = (oldList, newList, mappedList) -> {
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return oldList.size();
                }

                @Override
                public int getNewListSize() {
                    return newList.size();
                }

                @Override
                public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
                    final T oldItem = oldList.get(oldItemPosition);
                    final T newItem = newList.get(newItemPosition);
                    try {
                        return areSame.test(oldItem, newItem);
                    } catch (Exception ignored) {
                    }
                    return false;
                }

                @Override
                public boolean areContentsTheSame(final int oldItemPosition, final int newItemPosition) {
                    final T oldItem = oldList.get(oldItemPosition);
                    final T newItem = newList.get(newItemPosition);
                    return oldItem.equals(newItem);
                }
            });

            oldList.clear();
            oldList.addAll(newList);

            diffResult.dispatchUpdatesTo(new ListUpdateCallback() {
                @NonNull
                private List<R> generateMappedItems(final int position, final int count) {
                    final List<R> inserted = new ArrayList<>(count);
                    try {
                        for (int i = position; i < position + count; i++) {
                            final T item = oldList.get(i);
                            inserted.add(mapper.apply(item));
                        }
                    } catch (Exception ignored) {
                    }
                    return inserted;
                }

                @Override
                public void onInserted(final int position, final int count) {
                    mappedList.addAll(position, generateMappedItems(position, count));
                }

                @Override
                public void onRemoved(final int position, final int count) {
                    mappedList.subList(position, position + count).clear();
                }

                @Override
                public void onMoved(final int fromPosition, final int toPosition) {
                    final List<R> range = mappedList.subList(Math.min(fromPosition, toPosition), Math.max(fromPosition, toPosition));
                    Collections.rotate(range, fromPosition > toPosition ? 1 : -1);
                }

                @Override
                public void onChanged(final int position, final int count, final Object payload) {
                    final List<R> range = mappedList.subList(position, position + count);
                    range.clear();
                    range.addAll(generateMappedItems(position, count));
                }
            });
        };
    }

    public void update(final List<T> oldList, final List<T> newList, final List<R> mappedList) {
        theFunction.accept(oldList, newList, mappedList);
    }
}
