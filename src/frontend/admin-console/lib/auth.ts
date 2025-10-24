import type { NextAuthOptions } from "next-auth";
import type { OAuthConfig, OAuthUserConfig } from "next-auth/providers/oauth";

interface KeycloakProfile {
  sub: string;
  name?: string;
  preferred_username?: string;
  email?: string;
  picture?: string;
}

const keycloakBaseUrl = process.env.KEYCLOAK_BASE_URL || "http://localhost";
const keycloakInternalUrl = process.env.KEYCLOAK_INTERNAL_URL || "http://keycloak:8080";
const keycloakRealm = process.env.KEYCLOAK_REALM || "slms";
const nextAuthUrl = process.env.NEXTAUTH_URL || "http://localhost/admin";

const authorizationUrl = `${keycloakBaseUrl}/realms/${keycloakRealm}/protocol/openid-connect/auth`;
const tokenUrl = `${keycloakInternalUrl}/realms/${keycloakRealm}/protocol/openid-connect/token`;
const userinfoUrl = `${keycloakInternalUrl}/realms/${keycloakRealm}/protocol/openid-connect/userinfo`;
const jwksUrl = `${keycloakInternalUrl}/realms/${keycloakRealm}/protocol/openid-connect/certs`;
const issuer = `${keycloakBaseUrl}/realms/${keycloakRealm}`;
const callbackUrl = `${nextAuthUrl}/api/auth/callback/keycloak`;

export const authOptions: NextAuthOptions = {
  useSecureCookies: false,
  session: {
    strategy: "jwt",
  },
  providers: [
    {
      id: "keycloak",
      name: "Keycloak",
      type: "oauth",
      wellKnown: undefined,
      authorization: {
        url: authorizationUrl,
        params: {
          scope: "openid email profile",
          response_type: "code",
          redirect_uri: callbackUrl
        }
      },
      token: {
        url: tokenUrl,
        async request(context) {
          const body = new URLSearchParams({
            grant_type: "authorization_code",
            code: context.params.code as string,
            redirect_uri: callbackUrl,
            client_id: context.provider.clientId as string,
            client_secret: context.provider.clientSecret as string,
            code_verifier: context.checks.code_verifier || "",
          });

          const response = await fetch(tokenUrl, {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: body.toString(),
          });

          const tokens = await response.json();
          return { tokens };
        }
      },
      userinfo: userinfoUrl,
      jwks_endpoint: jwksUrl,
      issuer: issuer,
      clientId: process.env.KEYCLOAK_CLIENT_ID as string,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET as string,
      checks: ["pkce", "state"],
      profile(profile: KeycloakProfile) {
        return {
          id: profile.sub,
          name: profile.name ?? profile.preferred_username,
          email: profile.email,
          image: profile.picture,
        };
      },
    } as OAuthConfig<KeycloakProfile>,
  ],
  callbacks: {
    async redirect({ url, baseUrl }) {
      // Remove port from URLs (e.g., localhost:3000 -> localhost)
      const cleanUrl = url.replace(/:3000/, '');
      const cleanBaseUrl = baseUrl.replace(/:3000/, '');

      // Ensure redirects use /admin prefix
      if (cleanUrl.startsWith("/")) {
        return `${cleanBaseUrl}${cleanUrl}`;
      }
      if (cleanUrl.startsWith(cleanBaseUrl)) {
        return cleanUrl;
      }
      return cleanBaseUrl;
    },
    async jwt({ token, account }) {
      // Persist the OAuth access_token to the token right after signin
      if (account) {
        console.log('[Auth] JWT callback - Account found, storing tokens');
        token.accessToken = account.access_token as string;
        token.refreshToken = account.refresh_token as string;
        token.expiresAt = account.expires_at as number;
      }
      return token;
    },
    async session({ session, token }) {
      // Send properties to the client
      console.log('[Auth] Session callback - Token has accessToken:', !!token.accessToken);
      session.accessToken = token.accessToken as string | undefined;
      session.error = token.error as string | undefined;
      return session;
    },
  },
};
