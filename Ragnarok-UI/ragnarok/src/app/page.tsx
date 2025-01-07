import React from "react";
import Link from "next/link";
import { Separator } from "@/components/ui/separator";

const landingPage = () => {
  return (
    <>
      <header className="mb-4">
        <nav>
          <ul className="flex space-x-4">
            <li>
              <Link className="font-bold" href="/">
                Landing Page
              </Link>
            </li>
            <li>
              <Link className="font-bold" href="/chatApp">
                Chat
              </Link>
            </li>
          </ul>
        </nav>
      </header>

      <Separator />
      <div>
        <h1 className="text-4xl">Landing Page</h1>
        <p>This is the landing page content.</p>
      </div>
    </>
  );
};

export default landingPage;
