export type FilterKey = "ALL" | "CONFIRMED" | "LIKELY" | "POTENTIAL";

const TABS: { key: FilterKey; label: string }[] = [
  { key: "ALL", label: "All" },
  { key: "CONFIRMED", label: "Confirmed" },
  { key: "LIKELY", label: "Likely" },
  { key: "POTENTIAL", label: "Potential" },
];

export function FilterTabs({
  active,
  onChange,
}: {
  active: FilterKey;
  onChange: (key: FilterKey) => void;
}) {
  return (
    <div className="mb-6 flex gap-4 border-b border-white/5">
      {TABS.map((t) => {
        const isActive = t.key === active;
        return (
          <button
            key={t.key}
            onClick={() => onChange(t.key)}
            className={`relative px-1 pb-3 text-sm font-medium transition-colors duration-200 ${
              isActive ? "text-indigo-400" : "text-gray-400 hover:text-gray-200"
            }`}
          >
            {t.label}
            <span
              className={`absolute -bottom-px left-0 h-0.5 w-full origin-left bg-indigo-400 transition-transform duration-200 ${
                isActive ? "scale-x-100" : "scale-x-0"
              }`}
            />
          </button>
        );
      })}
    </div>
  );
}