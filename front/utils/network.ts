import Constants from 'expo-constants';

function isIPv4(host: string): boolean {
  return /^\d{1,3}(?:\.\d{1,3}){3}$/.test(host);
}

export function getLanHost(): string {
  try {
    // Prefer a configured host from app.json (extra) or expo public env
    const extraHost = (Constants as any)?.expoConfig?.extra?.apiHost || process.env.EXPO_PUBLIC_API_HOST;
    if (extraHost && typeof extraHost === 'string') {
      return extraHost;
    }

    // Fallback: parse hostUri only if it's an IPv4 address
    const hostUri = (Constants as any)?.expoConfig?.hostUri || (Constants as any)?.manifest2?.extra?.expoClient?.hostUri;
    if (hostUri && typeof hostUri === 'string') {
      const host = hostUri.split(':')[0];
      if (host && isIPv4(host)) return host;
    }
  } catch {}
  // Last resort
  return 'localhost';
}

export function buildUrl(port: number): string {
  const host = getLanHost();
  return `http://${host}:${port}`;
}
