import type { Metadata } from "next";
import { AppNav } from "@/components/nav/AppNav";
import "./globals.css";

export const metadata: Metadata = {
  title: "Test Management Platform",
  description: "Requirements, test cases, and execution tracking for your organization.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full flex flex-col">
        <AppNav />
        {children}
      </body>
    </html>
  );
}
