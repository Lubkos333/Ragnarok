import React from "react";
import Link from "next/link";
import { Button } from "../ui/button";
import HeroIllustration from "../hero-illustration";

interface HeroProps {
  id?: string;
}

export default function Hero({ id }: HeroProps) {
  return (
    <section
      id={id}
      className="w-full flex h-[80vh] bg-gradient-to-br from-accent-primary-mid to-accent-primary-dark text-primary-foreground pt-28 pb-8 px-6 justify-center"
    >
      <div className="grid grid-cols-1 lg:grid-cols-2 items-center h-full max-w-7xl">
        <div className="flex flex-col max-w-xl mx-auto gap-10">
          <h1 className="text-5xl max-w-2xl font-extrabold tracking-tight">
            RagNaRok: Váš AI právní asistent.
          </h1>
          <p className=" text-xl max-w-2xl">
            Už žádné složité hledání v zákonech a nejisté rady z internetu.
            RagNaRok Vám poskytne rychlé a relevantní odpovědi šité na míru Vaší
            situaci.
          </p>
          <Link href="/chatApp">
            <Button
              className="bg-background hover:bg-muted text-primary font-semibold"
              size="lg"
            >
              Vyzkoušet zdarma
            </Button>
          </Link>
        </div>
        <HeroIllustration className="hidden lg:block" />
      </div>
    </section>
  );
}
