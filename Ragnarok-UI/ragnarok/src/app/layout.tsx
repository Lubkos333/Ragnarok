import Link from "next/link";
import "./globals.css";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html>
      <body className="p-4">
        <header className="mb-4">
          <nav>
            <ul className="flex space-x-4">
              <li>
                <Link className="font-bold" href="/">
                  Landing Page
                </Link>
              </li>
              <li>
                <Link className="font-bold" href="/chat">
                  Chat
                </Link>
              </li>
            </ul>
          </nav>
        </header>
        {children}
      </body>
    </html>
  );
}
