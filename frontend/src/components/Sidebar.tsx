import { Link } from "@tanstack/react-router";

const navItems = [
  { label: "Overview", to: "/dashboard" },
  { label: "Subscriptions", to: "/dashboard" },
  { label: "Settings", to: "/dashboard" },
];

export function Sidebar() {
  return (
    <aside
      className="fixed left-0 top-0 h-screen w-60 border-r border-white/5 px-6 py-8"
      style={{ backgroundColor: "#0f0f1a" }}
    >
      <h1 className="mb-10 text-xl font-bold text-white">ShadowSpend</h1>
      <nav className="flex flex-col gap-2">
        {navItems.map((item) => (
          <Link
            key={item.label}
            to={item.to}
            className="rounded-md px-3 py-2 text-sm text-gray-300"
          >
            {item.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
}