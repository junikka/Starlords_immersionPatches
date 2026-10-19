package starlords.util;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Logger;

/**
 * Plays the rave's own music while its dialogs are open, the way vanilla's Luddic shrines do
 * (ShrineCMD playMusic/endMusic). The track is the "starlords_rave" music set in
 * data/config/sounds.json; swap the file there to change it.
 */
public final class RaveMusic {
    public static final String MUSIC_SET = "starlords_rave";
    // how long no dialog may be open before the failsafe gives the normal music back
    private static final long ORPHAN_MILLIS = 1500;
    private static final Logger log = Global.getLogger(RaveMusic.class);

    private static boolean enabled = true;
    private static boolean playing;
    private static Failsafe failsafe;

    private RaveMusic() {}

    public static void setEnabled(boolean value) {
        enabled = value;
        if (!value) stop();
    }

    /** Starts the rave music unless it's already playing; dialogs moving between rave screens keep it going. */
    public static void start() {
        if (!enabled || playing) return;
        try {
            Global.getSoundPlayer().playCustomMusic(1, 1, MUSIC_SET, true);
        } catch (RuntimeException e) {
            log.warn("Could not play music set " + MUSIC_SET, e);
            return;
        }
        playing = true;
        if (failsafe == null || failsafe.done) {
            failsafe = new Failsafe();
            Global.getSector().addTransientScript(failsafe);
        }
    }

    /** After a tournament fight, combat music has replaced ours. */
    public static void resume() {
        playing = false;
        start();
    }

    /** Hands control back to the normal campaign and market music. */
    public static void stop() {
        if (!playing) return;
        playing = false;
        Global.getSoundPlayer().setSuspendDefaultMusicPlayback(false);
        Global.getSoundPlayer().restartCurrentMusic();
    }

    // gives the normal music back if a rave dialog closed without stop(), e.g. through Esc
    private static class Failsafe implements EveryFrameScript {
        private boolean done;
        private long orphanedSince;

        @Override
        public void advance(float amount) {
            if (!playing) {
                done = true;
                return;
            }
            if (Global.getSector().getCampaignUI().getCurrentInteractionDialog() != null) {
                orphanedSince = 0;
                return;
            }
            long now = System.currentTimeMillis();
            if (orphanedSince == 0) orphanedSince = now;
            if (now - orphanedSince >= ORPHAN_MILLIS) {
                stop();
                done = true;
            }
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public boolean runWhilePaused() {
            return true;
        }
    }
}
