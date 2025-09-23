import { useState, useEffect, useContext, useRef } from 'react';
import { CurrentEmployeeSettingsContext } from './CurrentEmployeeSettingsContext';
import { CurrentEmployeeContext } from './CurrentEmployeeContext';

export const CurrentEmployeeSettingsProvider = ({ children }) => {
  const { currentEmployee } = useContext(CurrentEmployeeContext);
  
  const defaultSettings = {
    notifications: true,
    email: true,
    requestButtonSound: true,
    punchInSound: true,
    punchOutSound: true,
  };


  
  const [settings, setSettings] = useState(defaultSettings);
  const [loading, setLoading] = useState(false);

  const getSettingsKey = () => currentEmployee?._id ?` employee_settings_${currentEmployee._id}` : null;

  // Fetch settings from backend or fallback to memory
  useEffect(() => {
    const fetchSettings = async () => {
      const settingsKey = getSettingsKey();
      if (!settingsKey) return;

      setLoading(true);

      try {
        // Try fetching from backend
        const response = await fetch(`/api/employee/${currentEmployee._id}/settings`);
        if (!response.ok) throw new Error('Backend fetch failed');

        const data = await response.json();
        setSettings({ ...defaultSettings, ...data });

        // Save to memory fallback
        if (!window.employeeSettingsStorage) window.employeeSettingsStorage = {};
        window.employeeSettingsStorage[settingsKey] = data;

      } catch (error) {
        console.warn('Backend fetch failed, using fallback:', error);

        // Fallback to memory-based storage
        const fallback = window.employeeSettingsStorage?.[settingsKey] || defaultSettings;
        setSettings(fallback);
      } finally {
        setLoading(false);
      }
    };

    fetchSettings();
  }, [currentEmployee?._id]);

  const saveSettings = async (newSettings) => {
    const settingsKey = getSettingsKey();
    if (!settingsKey) return;

    setLoading(true);

    try {
      // Try saving to backend
      const response = await fetch(`/api/employee/${currentEmployee._id}/settings`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(newSettings),
      });

      if (!response.ok) throw new Error('Backend save failed');

      // Update memory fallback
      if (!window.employeeSettingsStorage) window.employeeSettingsStorage = {};
      window.employeeSettingsStorage[settingsKey] = newSettings;

      setSettings(newSettings);

    } catch (error) {
      console.warn('Backend save failed, using fallback:', error);

      // Save only to memory fallback
      if (!window.employeeSettingsStorage) window.employeeSettingsStorage = {};
      window.employeeSettingsStorage[settingsKey] = newSettings;

      setSettings(newSettings);
    } finally {
      setLoading(false);
    }
  };

  const updateSetting = async (key, value) => {
    const newSettings = { ...settings, [key]: value };
    await saveSettings(newSettings);
  };

  const toggleSetting = async (key) => {
    const newSettings = { ...settings, [key]: !settings[key] };
    setSettings(newSettings); // Optimistic update
    try {
      await updateSetting(key, newSettings[key]);
    } catch {
      const settingsKey = getSettingsKey();
      setSettings(window.employeeSettingsStorage?.[settingsKey] || defaultSettings);
    }
  };

  const resetSettings = async () => {
    await saveSettings(defaultSettings);
  };

    // audioRef will hold preloaded Audio objects
  const audioRef = useRef({});

  // Map logical soundType -> primary filename(s)
  const audioFiles = {
    punchIn: ['/sounds/punched-in.mp3', '/sounds/punched-in.wav'],
    punchOut: ['/sounds/punch-out.mp3', '/sounds/punch-out.wav'],
    request: ['/sounds/request-button.mp3', '/sounds/request-button.wav'], // optional request sound
  };

  // Preload primary audio files once
  useEffect(() => {
    Object.keys(audioFiles).forEach((key) => {
      const src = audioFiles[key][0];
      const a = new Audio(src);
      a.preload = 'auto';
      audioRef.current[key] = a;
    });
  }, []);

  // Helper fallback oscillator (used if playing the actual file fails)
  const fallbackBeep = (type) => {
    try {
      const audioContext = new (window.AudioContext || window.webkitAudioContext)();
      const oscillator = audioContext.createOscillator();
      const gainNode = audioContext.createGain();
      oscillator.connect(gainNode);
      gainNode.connect(audioContext.destination);
      oscillator.frequency.value = type === 'punchIn' ? 800 : 600;
      oscillator.type = 'sine';
      gainNode.gain.setValueAtTime(0.3, audioContext.currentTime);
      gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5);
      oscillator.start(audioContext.currentTime);
      oscillator.stop(audioContext.currentTime + 0.5);
    } catch (err) {
      console.error('Oscillator fallback failed:', err);
    }
  };

  const playSound = async (soundType) => {
    try {
      // Map incoming soundType to settings key
      const settingMap = {
        request: 'requestButtonSound',
        punchIn: 'punchInSound',
        punchOut: 'punchOutSound',
      };
      const settingKey = settingMap[soundType] || soundType;

      // If the user disabled this specific sound, do nothing
      if (settings && typeof settings[settingKey] !== 'undefined' && !settings[settingKey]) {
        console.log('Sound disabled by settings:', settingKey);
        return;
      }

      // Get (or lazily create) an Audio element for this sound
      let audio = audioRef.current[soundType];
      if (!audio) {
        const candidates = audioFiles[soundType] || [`/sounds/${soundType}.mp3`];
        audio = new Audio(candidates[0]);
        audio.preload = 'auto';
        audioRef.current[soundType] = audio;
      }

      // Use cloneNode so multiple rapid plays don't interrupt each other
      const audioToPlay = audio.cloneNode(true);
      audioToPlay.currentTime = 0;
      // Set a sensible volume if you like: audioToPlay.volume = 0.8;
      await audioToPlay.play();
    } catch (err) {
      console.warn('Playing audio file failed, using fallback beep.', err);
      fallbackBeep(soundType);
    }
  };

  return (
    <CurrentEmployeeSettingsContext.Provider value={{
      settings,
      loading,
      updateSetting,
      toggleSetting,
      resetSettings,
      saveSettings,
      playSound,
      notificationsEnabled: settings.notifications,
    }}>
      {children}
    </CurrentEmployeeSettingsContext.Provider>
  );
};