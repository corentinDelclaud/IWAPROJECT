import React, { useMemo, useState } from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { Colors } from '@/constants/theme';
import { useColorScheme } from '@/hooks/use-color-scheme';
import i18n from '@/i18n';
import LanguageSwitcher from '@/components/LanguageSwitcher';

export function MobileHeader() {
  const colorScheme = useColorScheme() ?? 'dark';
  const theme = Colors[colorScheme];
  const [current] = useState(i18n.language);

  return (
    <View style={{
      backgroundColor: 'rgba(2,6,23,0.6)',
      borderBottomWidth: 1,
      borderBottomColor: 'rgba(168,85,247,0.2)',
      paddingHorizontal: 16,
      paddingVertical: 12,
    }}>
      <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' }}>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
          <Ionicons name="game-controller" size={20} color="#A78BFA" />
          <Text style={{
            fontWeight: '700',
            fontSize: 18,
            backgroundClip: 'text' as any,
            color: '#C4B5FD',
          }}>Next Level</Text>
        </View>

        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
          <TouchableOpacity style={{ padding: 8, borderRadius: 10, backgroundColor: 'rgba(51,65,85,0.4)' }}>
            <Ionicons name="search" size={18} color={theme.text} />
          </TouchableOpacity>
          <View>
            <TouchableOpacity style={{ padding: 8, borderRadius: 10, backgroundColor: 'rgba(51,65,85,0.4)' }}>
              <Ionicons name="notifications-outline" size={18} color={theme.text} />
            </TouchableOpacity>
            <View style={{ position: 'absolute', top: -2, right: -2, width: 14, height: 14, borderRadius: 7, backgroundColor: '#ef4444', alignItems: 'center', justifyContent: 'center' }}>
              <Text style={{ color: 'white', fontSize: 10, fontWeight: '700' }}>3</Text>
            </View>
          </View>
          {/* <LanguageSwitcher /> */}
          <View style={{ width: 32, height: 32, borderRadius: 16, backgroundColor: 'rgba(147,51,234,0.5)', alignItems: 'center', justifyContent: 'center' }}>
            <Text style={{ color: 'white', fontWeight: '700' }}>G</Text>
          </View>
        </View>
      </View>
      
    </View>
  );
}


