package com.example.androidstudio2dgamedevelopment;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Wraps Firebase Auth (anonymous) + Firestore access for the online top-10 leaderboard. */
public class LeaderboardManager {

    private static final String COLLECTION = "leaderboard";
    private static final int NAME_MAX_LENGTH = 20;

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public LeaderboardManager() {
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void ensureSignedIn(Runnable onReady, Consumer<Exception> onError) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            onReady.run();
            return;
        }
        auth.signInAnonymously()
                .addOnSuccessListener(result -> onReady.run())
                .addOnFailureListener(onFailure(onError));
    }

    /** Submits (or raises) the player's personal-best score under their anonymous UID. */
    public void submitScore(String name, long score, Runnable onSuccess, Consumer<Exception> onError) {
        String trimmedName = name == null ? "" : name.trim();
        if (trimmedName.isEmpty()) trimmedName = "Anonym";
        if (trimmedName.length() > NAME_MAX_LENGTH) trimmedName = trimmedName.substring(0, NAME_MAX_LENGTH);
        String finalName = trimmedName;

        ensureSignedIn(() -> {
            String uid = auth.getCurrentUser().getUid();
            Map<String, Object> data = new HashMap<>();
            data.put("name", finalName);
            data.put("score", score);
            data.put("updatedAt", FieldValue.serverTimestamp());
            firestore.collection(COLLECTION).document(uid)
                    .set(data)
                    .addOnSuccessListener(unused -> onSuccess.run())
                    .addOnFailureListener(onFailure(onError));
        }, onError);
    }

    /** Fetches the top {@code limit} scores, ordered highest first. */
    public void fetchTop(int limit, Consumer<List<LeaderboardEntry>> onResult, Consumer<Exception> onError) {
        firestore.collection(COLLECTION)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<LeaderboardEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String name = doc.getString("name");
                        Long score = doc.getLong("score");
                        if (name != null && score != null) {
                            entries.add(new LeaderboardEntry(name, score));
                        }
                    }
                    onResult.accept(entries);
                })
                .addOnFailureListener(onFailure(onError));
    }

    @NonNull
    private OnFailureListener onFailure(Consumer<Exception> onError) {
        return e -> {
            if (onError != null) onError.accept(e);
        };
    }

    /** A single leaderboard row. */
    public static class LeaderboardEntry {
        public final String name;
        public final long score;

        public LeaderboardEntry(String name, long score) {
            this.name = name;
            this.score = score;
        }
    }
}
