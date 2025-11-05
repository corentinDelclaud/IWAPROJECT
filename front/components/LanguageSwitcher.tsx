// typescript
// File: `front/components/LanguageSwitcher.tsx`
// Guard loading of `react-native-ico-flags` on web and fallback to emoji.

import React, { useMemo, useState } from 'react';
import { View, Text, TouchableOpacity, Modal, Pressable, Platform } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import i18n, { setStoredLanguage } from '@/i18n';
import manifest from '@/locales/languages.json';

let FlagIcon: any = null;
if (Platform.OS !== 'web') {
    try {
        // eslint-disable-next-line @typescript-eslint/no-var-requires
        const pkg = require('react-native-ico-flags');
        FlagIcon = pkg?.default || pkg?.Icon || pkg || null;
    } catch (e) {
        FlagIcon = null;
    }
} else {
    FlagIcon = null;
}

const countryCodeToEmoji = (code?: string) => {
    if (!code) return undefined;
    const normalized = code.trim().toUpperCase();
    if (/^[A-Z]{2}$/.test(normalized)) {
        return Array.from(normalized)
            .map((ch) => 0x1f1e6 + (ch.charCodeAt(0) - 65))
            .map((cp) => String.fromCodePoint(cp))
            .join('');
    }
    return undefined;
};

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

    const renderFlag = (lng: string) => {
        const flagName = flagNameByLng?.[lng] || undefined;
        if (FlagIcon) {
            // try common prop shapes; adjust if your native package expects different props
            return <FlagIcon name={flagName || 'flag'} height={18} width={18} />;
        }
        const emojiFromCode = countryCodeToEmoji(flagName);
        const emojiFallbacks: Record<string, string> = { fr: '🇫🇷', en: '🇬🇧' };
        const emoji = emojiFromCode || emojiFallbacks[lng] || '🏳️';
        return <Text style={{ fontSize: 18 }}>{emoji}</Text>;
    };

    return (
        <View>
            <TouchableOpacity
                onPress={() => setPickerVisible(true)}
                style={{
                    paddingHorizontal: 10,
                    paddingVertical: 6,
                    borderRadius: 10,
                    backgroundColor: 'rgba(51,65,85,0.4)',
                    flexDirection: 'row',
                    alignItems: 'center',
                    gap: 6,
                }}
            >
                {renderFlag(current)}
            </TouchableOpacity>

            <Modal visible={pickerVisible} transparent animationType="fade" onRequestClose={() => setPickerVisible(false)}>
                <Pressable
                    style={{ flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'flex-start' }}
                    onPress={() => setPickerVisible(false)}
                >
                    <View
                        style={{
                            marginTop: 60,
                            marginHorizontal: 12,
                            borderRadius: 12,
                            overflow: 'hidden',
                            borderWidth: 1,
                            borderColor: 'rgba(168,85,247,0.3)',
                        }}
                    >
                        <View style={{ backgroundColor: 'rgba(2,6,23,0.95)' }}>
                            {languages.map((lng) => (
                                <Pressable
                                    key={lng}
                                    onPress={() => onSelect(lng)}
                                    style={{
                                        paddingVertical: 12,
                                        paddingHorizontal: 14,
                                        flexDirection: 'row',
                                        alignItems: 'center',
                                        justifyContent: 'space-between',
                                        borderBottomWidth: 1,
                                        borderBottomColor: 'rgba(51,65,85,0.4)',
                                    }}
                                >
                                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                                        {renderFlag(lng)}
                                        <Text style={{ color: '#e5e7eb', fontWeight: current === lng ? ('700' as any) : ('500' as any) }}>
                                            {labels[lng] || lng}
                                        </Text>
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