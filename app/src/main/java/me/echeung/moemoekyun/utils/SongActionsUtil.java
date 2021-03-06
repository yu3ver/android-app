package me.echeung.moemoekyun.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import me.echeung.listenmoeapi.callbacks.FavoriteSongCallback;
import me.echeung.listenmoeapi.callbacks.RequestSongCallback;
import me.echeung.listenmoeapi.models.Song;
import me.echeung.listenmoeapi.responses.Messages;
import me.echeung.moemoekyun.App;
import me.echeung.moemoekyun.R;
import me.echeung.moemoekyun.ui.fragments.UserFragment;

public class SongActionsUtil {

    /**
     * Updates the favorite status of a song.
     *
     * @param song The song to update the favorite status of.
     */
    public static void favorite(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        final int songId = song.getId();
        App.getApiClient().favoriteSong(songId, new FavoriteSongCallback() {
            @Override
            public void onSuccess(final boolean favorited) {
                if (App.getRadioViewModel().getCurrentSong().getId() == songId) {
                    App.getRadioViewModel().setIsFavorited(favorited);
                }

                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        song.setFavorite(favorited);
                        adapter.notifyDataSetChanged();

                        // Broadcast event
                        final Intent favIntent = new Intent(UserFragment.FAVORITE_EVENT);
                        activity.sendBroadcast(favIntent);

                        // Undo action
                        if (!favorited) {
                            final View coordinatorLayout = activity.findViewById(R.id.coordinator_layout);
                            if (coordinatorLayout != null) {
                                final Snackbar undoBar = Snackbar.make(coordinatorLayout,
                                        String.format(activity.getString(R.string.unfavorited), song.getTitle()),
                                        Snackbar.LENGTH_LONG);
                                undoBar.setAction(R.string.action_undo, (v) -> favorite(activity, adapter, song));
                                undoBar.show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(final String message) {
                if (activity != null) {
                    activity.runOnUiThread(() -> Toast.makeText(activity.getApplicationContext(), R.string.error, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /**
     * Requests a song.
     *
     * @param song The song to request.
     */
    public static void request(final Activity activity, final RecyclerView.Adapter adapter, final Song song) {
        final int requests = App.getUserViewModel().getUserRequests();
        if (requests <= 0) {
            Toast.makeText(activity.getApplicationContext(), R.string.no_requests_left, Toast.LENGTH_SHORT).show();
            return;
        }

        App.getApiClient().requestSong(song.getId(), new RequestSongCallback() {
            @Override
            public void onSuccess() {
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        song.setEnabled(false);
                        adapter.notifyDataSetChanged();

                        // Broadcast event
                        final Intent reqEvent = new Intent(UserFragment.REQUEST_EVENT);
                        activity.sendBroadcast(reqEvent);

                        final int remainingReqs = requests - 1;
                        App.getUserViewModel().setUserRequests(remainingReqs);

                        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.requested_song, song.getArtist()), Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onFailure(final String message) {
                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        final int toastMsg = message.equals(Messages.USER_NOT_SUPPORTER) ?
                                R.string.supporter_required :
                                R.string.error;

                        Toast.makeText(activity.getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    public static void copyToClipboard(final Context context, final Song song) {
        copyToClipboard(context, song.toString());
    }

    public static void copyToClipboard(final Context context, final String songInfo) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("song", songInfo);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
    }
}
