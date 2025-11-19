/**
 * Below are the colors that are used in the app. The colors are defined in the light and dark mode.
 * There are many other ways to style your app. For example, [Nativewind](https://www.nativewind.dev/), [Tamagui](https://tamagui.dev/), [unistyles](https://reactnativeunistyles.vercel.app), etc.
 */

import { Platform } from 'react-native';

const tintColorLight = '#0a7ea4';
const tintColorDark = '#fff';

export const Colors = {
    light: {
        text: '#ECEDEE',
        background: '#151718',
        tint: tintColorDark,
        icon: '#9BA1A6',
        tabIconDefault: '#9BA1A6',
        tabIconSelected: tintColorDark,
        slateBg: '#0b1220',
        slateCard: 'rgba(30,41,59,0.5)',
        slateBorder: 'rgba(51,65,85,0.5)',
        purple: '#8b5cf6',
        blue: '#3b82f6',
        yellow: '#fbbf24',
        green: '#22c55e',
        card: '#ffffff',
        primary: '#2f95dc',
        textSecondary: '#6b7280',
    },
    dark: {
        text: '#ECEDEE',
        textSecondary: '#9ca3af',
        background: '#151718',
        tint: tintColorDark,
        icon: '#9BA1A6',
        tabIconDefault: '#9BA1A6',
        tabIconSelected: tintColorDark,
        slateBg: '#0b1220',
        slateCard: 'rgba(30,41,59,0.5)',
        slateBorder: 'rgba(51,65,85,0.5)',
        purple: '#8b5cf6',
        blue: '#3b82f6',
        yellow: '#fbbf24',
        green: '#22c55e',
            gradientStart: '#0b1220',
            gradientEnd: '#151718',
        card: '#1e293b',
        primary: '#60a5fa',
    },
};

export const Fonts = Platform.select({
    ios: {
        /** iOS `UIFontDescriptorSystemDesignDefault` */
        sans: 'system-ui',
        /** iOS `UIFontDescriptorSystemDesignSerif` */
        serif: 'ui-serif',
        /** iOS `UIFontDescriptorSystemDesignRounded` */
        rounded: 'ui-rounded',
        /** iOS `UIFontDescriptorSystemDesignMonospaced` */
        mono: 'ui-monospace',
    },
    default: {
        sans: 'normal',
        serif: 'serif',
        rounded: 'normal',
        mono: 'monospace',
    },
    web: {
        sans: "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
        serif: "Georgia, 'Times New Roman', serif",
        rounded: "'SF Pro Rounded', 'Hiragino Maru Gothic ProN', Meiryo, 'MS PGothic', sans-serif",
        mono: "SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace",
    },
});
