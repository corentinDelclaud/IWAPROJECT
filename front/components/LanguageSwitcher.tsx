import React, { useMemo, useState } from 'react';
import { View, Text, TouchableOpacity, Modal, Pressable } from 'react-native';
import Icon from 'react-native-ico-flags';
import { Ionicons } from '@expo/vector-icons';
import i18n, { setStoredLanguage } from '@/i18n';
import manifest from '@/locales/languages.json';

export const LanguageSwitcher: React.FC = () => {
  const [pickerVisible, setPickerVisible] = useState(false);
  const [current, setCurrent] = useState(i18n.language);
  const languages = useMemo(() => manifest.supportedLngs, []);
  const labels = (manifest.labels || {}) as Record<string, string>;
  const flagNameByLng = (manifest as any).flagNameByLng as Record<string, string>;

  const onSelect = async (lng: string) => {
    await i18n.changeLanguage(lng);
    await setStoredLanguage(lng);
    setCurrent(lng);
    setPickerVisible(false);
  };

  return (
    <View>
      <TouchableOpacity onPress={() => setPickerVisible(true)} style={{ paddingHorizontal: 10, paddingVertical: 6, borderRadius: 10, backgroundColor: 'rgba(51,65,85,0.4)', flexDirection: 'row', alignItems: 'center', gap: 6 }}>
        <Icon name={flagNameByLng?.[current] || 'flag'} height={18} width={18} />
      </TouchableOpacity>
      <Modal visible={pickerVisible} transparent animationType="fade" onRequestClose={() => setPickerVisible(false)}>
        <Pressable style={{ flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'flex-start' }} onPress={() => setPickerVisible(false)}>
          <View style={{ marginTop: 60, marginHorizontal: 12, borderRadius: 12, overflow: 'hidden', borderWidth: 1, borderColor: 'rgba(168,85,247,0.3)' }}>
            <View style={{ backgroundColor: 'rgba(2,6,23,0.95)' }}>
              {languages.map((lng) => (
                <Pressable key={lng} onPress={() => onSelect(lng)} style={{ paddingVertical: 12, paddingHorizontal: 14, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', borderBottomWidth: 1, borderBottomColor: 'rgba(51,65,85,0.4)' }}>
                  <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                    <Icon name={flagNameByLng?.[lng] || 'flag'} height={18} width={18} />
                    <Text style={{ color: '#e5e7eb', fontWeight: current === lng ? '700' as any : '500' }}>{labels[lng] || lng}</Text>
                  </View>
                  {current === lng && <Ionicons name="checkmark" size={16} color="#A78BFA" />}
                </Pressable>
              ))}
            </View>
          </View>
        </Pressable>
      </Modal>
    </View>
  );
};

export default LanguageSwitcher;


