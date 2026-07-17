interface LoadingProps {
  message?: string;
}

function Loading({ message = "Loading…" }: LoadingProps) {
  return (
    <div className="app-card">
      <h2>{message}</h2>
    </div>
  );
}

export default Loading;
