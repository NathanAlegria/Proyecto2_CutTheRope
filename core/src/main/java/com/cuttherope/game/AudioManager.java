

package com.cuttherope.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import java.util.HashMap;
import java.util.Map;


public class AudioManager {

    private static AudioManager instance;


    public static final String MUSIC_MENU = "audio/menu_theme.mp3";
    public static final String MUSIC_GAME = "audio/game_theme.mp3";


    public static final String SFX_CUT   = "audio/cut.wav";
    public static final String SFX_EAT   = "audio/eat.wav";
    public static final String SFX_STAR  = "audio/star.wav";
    public static final String SFX_WIN   = "audio/win.wav";
    public static final String SFX_LOSE  = "audio/lose.wav";
    public static final String SFX_CLICK = "audio/click.wav";

    private Music currentMusic;
    private String currentMusicPath = "";
    private String pendingMusicPath = MUSIC_MENU;

    private float musicVolume = 0.7f;
    private float sfxVolume   = 0.8f;

    private boolean musicEnabled = true;
    private boolean sfxEnabled   = true;

    private final Map<String, Sound> soundCache = new HashMap<>();


    private Thread preloadThread;
    private volatile boolean preloadComplete = false;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }

        return instance;
    }


    public void preloadAsync() {
        if (preloadThread != null && preloadThread.isAlive()) {
            return;
        }

        preloadThread = new Thread(() -> {
            String[] files = {
                MUSIC_MENU,
                MUSIC_GAME,
                SFX_CUT,
                SFX_EAT,
                SFX_STAR,
                SFX_WIN,
                SFX_LOSE,
                SFX_CLICK
            };

            for (String file : files) {
                try {
                    if (!Gdx.files.internal(file).exists()) {
                        System.out.println("[AudioManager] Falta asset de audio: " + file);
                    }
                } catch (Exception e) {
                    System.err.println("[AudioManager] Error verificando audio: " + file);
                }
            }

            preloadComplete = true;
            System.out.println("[AudioManager] Precarga/verificación completada.");
        }, "AudioPreloadThread");

        preloadThread.setDaemon(true);
        preloadThread.start();
    }


    public void playMenuMusic() {
        playMusic(MUSIC_MENU);
    }

    public void playGameMusic() {
        playMusic(MUSIC_GAME);
    }


    public void playMusic(String path) {
        pendingMusicPath = path;

        if (!musicEnabled) {
            return;
        }

        try {
            if (!Gdx.files.internal(path).exists()) {
                System.err.println("[AudioManager] No existe música: " + path);
                return;
            }


            if (currentMusic != null && path.equals(currentMusicPath)) {
                currentMusic.setVolume(musicVolume);

                if (!currentMusic.isPlaying()) {
                    currentMusic.play();
                }

                return;
            }


            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic.dispose();
                currentMusic = null;
            }

            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
            currentMusicPath = path;

            currentMusic.setLooping(true);
            currentMusic.setVolume(musicVolume);
            currentMusic.play();

        } catch (Exception e) {
            System.err.println("[AudioManager] No se pudo cargar música: " + path);
            System.err.println("[AudioManager] Detalle: " + e.getMessage());
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (!musicEnabled) {
            return;
        }

        if (currentMusic != null) {
            currentMusic.play();
        } else if (pendingMusicPath != null && !pendingMusicPath.isEmpty()) {
            playMusic(pendingMusicPath);
        }
    }


    public void playSound(String path) {
        if (!sfxEnabled) {
            return;
        }

        try {
            Sound sound = soundCache.get(path);

            if (sound == null) {
                if (!Gdx.files.internal(path).exists()) {
                    System.err.println("[AudioManager] No existe sonido: " + path);
                    return;
                }

                sound = Gdx.audio.newSound(Gdx.files.internal(path));
                soundCache.put(path, sound);
            }

            sound.play(sfxVolume);

        } catch (Exception e) {
            System.err.println("[AudioManager] No se pudo reproducir sonido: " + path);
            System.err.println("[AudioManager] Detalle: " + e.getMessage());
        }
    }

    public void playCut() {
        playSound(SFX_CUT);
    }

    public void playEat() {
        playSound(SFX_EAT);
    }

    public void playStar() {
        playSound(SFX_STAR);
    }

    public void playWin() {
        playSound(SFX_WIN);
    }

    public void playLose() {
        playSound(SFX_LOSE);
    }

    public void playClick() {
        playSound(SFX_CLICK);
    }


    public void setMusicVolume(float volume) {
        musicVolume = clamp01(volume);

        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clamp01(volume);
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }


    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public boolean isSfxEnabled() {
        return sfxEnabled;
    }

    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;

        if (!musicEnabled) {
            stopMusic();
        } else {
            resumeMusic();
        }
    }

    public void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;
    }


    public void applyUserPrefs(UserData ud) {
        if (ud == null) {
            return;
        }

        setMusicVolume(ud.getMusicVolume());
        setSfxVolume(ud.getSfxVolume());
        setMusicEnabled(ud.isMusicEnabled());
        setSfxEnabled(ud.isSfxEnabled());
    }

    public boolean isPreloadComplete() {
        return preloadComplete;
    }


    private float clamp01(float value) {
        if (value < 0f) {
            return 0f;
        }

        if (value > 1f) {
            return 1f;
        }

        return value;
    }


    public void dispose() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }

        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }

        soundCache.clear();
    }
}
