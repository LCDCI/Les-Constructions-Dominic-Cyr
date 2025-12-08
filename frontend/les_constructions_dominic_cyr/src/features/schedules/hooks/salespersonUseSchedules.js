import { useState, useEffect } from 'react';
import scheduleApi from '../api/salespersonScheduleApi';

const salespersonUseSchedules = (fetchAll = false) => {
    const [schedules, setSchedules] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchSchedules = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = fetchAll
                ? await scheduleApi.getAllSchedules()
                : await scheduleApi.getCurrentWeekSchedules();
            setSchedules(data);
        } catch (err) {
            setError(err.message || 'Failed to load schedules');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSchedules();
    }, [fetchAll]);

    const refetch = () => {
        fetchSchedules();
    };

    return { schedules, loading, error, refetch };
};

export default salespersonUseSchedules;