import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

export async function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Allow access to auth routes
  // With basePath, Next.js strips /admin before middleware, so we check without it
  if (pathname.startsWith("/admin/api/auth") || pathname.startsWith("/api/auth")) {
    return NextResponse.next();
  }

  // Check if user has access token in cookies (set from landing page)
  const accessToken = request.cookies.get("access_token");

  // If no access token, redirect to landing page for login
  if (!accessToken) {
    const landingPageUrl = new URL("/", request.url);
    return NextResponse.redirect(landingPageUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except:
     * - api/auth (NextAuth routes)
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     * - public folder
     */
    "/((?!api/auth|_next/static|_next/image|favicon.ico|.*\\..*).+)",
  ],
};
