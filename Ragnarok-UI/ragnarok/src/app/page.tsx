import React from "react";
import Link from "next/link";

import { Button } from "@/components/ui/button";

import Hero from "@/components/productPage/Hero";
import UseCases from "@/components/productPage/UseCases";
import Features from "@/components/productPage/Features";
import Testimonials from "@/components/productPage/Testimonials";
import Faq from "@/components/productPage/Faq";
import LogoIcon from "@/components/logo-icon";

import HowItWorks from "@/components/productPage/HowItWorks";
import Pricing from "@/components/productPage/Pricing";

const landingPage = () => {
  return (
    <>
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
              href="#usecases"
              className="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-primary hover:border-accent-primary"
            >
              Použití
            </Link>
            <Link
              href="#howitworks"
              className="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-primary hover:border-accent-primary"
            >
              Jak to funguje
            </Link>
            <Link
              href="#pricing"
              className="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-primary hover:border-accent-primary"
            >
              Ceník
            </Link>
            <Link
              href="#faq"
              className="inline-flex items-center px-1 pt-1 border-b-2 border-transparent text-sm font-medium text-primary hover:border-accent-primary"
            >
              FAQ
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

      <Hero />
      <Features />
      <UseCases id="usecases" />
      <HowItWorks id="howitworks" />
      <Testimonials />
      <Pricing id="pricing" />
      <Faq id="faq" />

      <footer className="bg-slate-900 text-slate-300 py-8 text-center mt-16">
        <p className="text-sm">&copy; 2023 Ragnarok. All rights reserved.</p>
        <div className="mt-3 space-x-4">
          <Link href="/privacy" className="hover:text-slate-100">
            Privacy Policy
          </Link>
          <Link href="/terms" className="hover:text-slate-100">
            Terms of Service
          </Link>
        </div>
      </footer>
    </>
  );
};

export default landingPage;
