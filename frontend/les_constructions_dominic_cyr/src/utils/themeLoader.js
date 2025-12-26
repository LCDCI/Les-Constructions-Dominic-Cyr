export async function loadTheme() {
  const res = await fetch("http://localhost:8080/api/theme");
  const theme = await res.json();

  const root = document.documentElement;

  root.style.setProperty("--primary-color", theme.primaryColor);
  root.style.setProperty("--secondary-color", theme.secondaryColor);
  root.style.setProperty("--accent-color", theme.accentColor);
  root.style.setProperty("--card-background", theme.cardBackground);
  root.style.setProperty("--background-color", theme.backgroundColor);
  root.style.setProperty("--text-primary", theme.textPrimary);
  root.style.setProperty("--white", theme.white);

  root.style.setProperty("--border-radius", theme.borderRadius);
  root.style.setProperty("--box-shadow", theme.boxShadow);
  root.style.setProperty("--transition", theme.transition);
}
