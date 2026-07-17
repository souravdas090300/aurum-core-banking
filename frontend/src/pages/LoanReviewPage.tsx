import { useEffect, useState } from "react";
import { useKeycloak } from "@react-keycloak/web";
import toast from "react-hot-toast";
import { fetchLoanTasks, reviewLoanTask } from "../api/banking";
import { LoanTask, LoanReviewRequest } from "../types";
import Loading from "../components/Loading";

function LoanReviewPage() {
  const { keycloak } = useKeycloak();
  const token = keycloak?.token;
  const [tasks, setTasks] = useState<LoanTask[]>([]);
  const [loading, setLoading] = useState(true);
  const [notes, setNotes] = useState<Record<number, string>>({});

  useEffect(() => {
    async function loadTasks() {
      try {
        const taskData = await fetchLoanTasks(token);
        setTasks(taskData);
      } catch (error) {
        toast.error("Unable to load loan review tasks.");
      } finally {
        setLoading(false);
      }
    }
    loadTasks();
  }, [token]);

  const handleReview = async (taskId: number, approved: boolean) => {
    const request: LoanReviewRequest = {
      approved,
      notes: notes[taskId] || "Reviewed by team",
    };

    try {
      await reviewLoanTask(taskId, request, token);
      toast.success("Loan review updated.");
      setTasks((current) => current.filter((task) => task.id !== taskId));
    } catch (error) {
      toast.error("Failed to submit loan review.");
    }
  };

  if (loading) {
    return <Loading message="Loading loan review queue…" />;
  }

  return (
    <div className="app-card">
      <h2>Loan Review Queue</h2>
      {tasks.length === 0 ? (
        <p>No pending loan review tasks. All applications are up to date.</p>
      ) : (
        tasks.map((task) => (
          <div key={task.id} className="app-card" style={{ marginBottom: "16px" }}>
            <h3>{task.name}</h3>
            <p>
              <strong>Status:</strong> {task.status}
            </p>
            <label>
              Notes
              <textarea
                rows={3}
                value={notes[task.id] || ""}
                onChange={(event) =>
                  setNotes((current) => ({ ...current, [task.id]: event.target.value }))
                }
              />
            </label>
            <div style={{ display: "flex", gap: "12px" }}>
              <button type="button" onClick={() => handleReview(task.id, true)}>
                Approve
              </button>
              <button type="button" onClick={() => handleReview(task.id, false)}>
                Decline
              </button>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default LoanReviewPage;
