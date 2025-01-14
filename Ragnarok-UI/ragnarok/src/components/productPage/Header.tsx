import React from "react";
import Link from "next/link";
import LogoIcon from "../logo-icon";
import { Button } from "../ui/button";

export default function Header() {
  return (
    <header>
      <nav className="flex fixed w-full h-16 bg-background justify-around items-center py-4">
        <div className="flex-shrink-0 flex items-center">
          <Link className="flex flex-row gap-2" href="/">
            <div className="flex aspect-square size-8 items-center justify-center rounded-lg ">
              <LogoIcon className="text-primary" />
            </div>
            <div className="flex flex-col leading-none gap-0.5">
              <span className="font-semibold">Ragnarok</span>
              <span className="text-xs">Beta</span>
            </div>
          </Link>
        </div>
        <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
          <Link
            href="/"
            className="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-primary hover:border-accent-primary"
          >
            Home
          </Link>
          <Link
            href="/features"
            className="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-primary hover:border-accent-primary"
          >
            Features
          </Link>
          <Link
            href="/pricing"
            className="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-primary hover:border-accent-primary"
          >
            Pricing
          </Link>
          <Link
            href="/contact"
            className="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-primary hover:border-accent-primary"
          >
            Contact
          </Link>
        </div>
        <div className="flex items-center">
          <Link href="/chatApp" className="text">
            <Button
              className="text-sm font-medium text-background dark:text-foreground bg-accent-primary hover:bg-accent-primary-mid"
              size="sm"
            >
              Vyzkoušet zdarma
            </Button>
          </Link>
        </div>
      </nav>
    </header>
  );
}
