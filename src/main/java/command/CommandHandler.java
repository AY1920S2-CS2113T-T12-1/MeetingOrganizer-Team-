package command;

import exception.InvalidUrlException;
import meeting.MeetingList;
import exception.MoException;
import meeting.Meeting;
import modulelogic.LessonsGenerator;
import schedulelogic.ScheduleHandler;
import schedulelogic.TeamMember;
import schedulelogic.TeamMemberList;
import ui.TextUI;

import java.time.LocalTime;
import java.util.ArrayList;

public class CommandHandler {

    public static TeamMember addContact(TeamMemberList myTeamMemberList, String[] userInputWords,
                                        Integer startDay, Integer endDay) throws MoException {
        TeamMember member;
        int checkerForRepeatedName;
        checkerForRepeatedName = myTeamMemberList.getTeamMemberList().stream()
                .mapToInt(person -> check(person, userInputWords[0])).sum();
        if (checkerForRepeatedName == 1) {
            TextUI.showRepeatedPerson(userInputWords[0]);
            throw new MoException("Repeated user");
        }

        member = new TeamMember(userInputWords[0]);
        String name = userInputWords[0];
        LessonsGenerator myLessonGenerator;
        try {
            myLessonGenerator = new LessonsGenerator(userInputWords[1]);
            myLessonGenerator.generate();
            ArrayList<String[]> myLessonDetails = myLessonGenerator.getLessonDetails();
            for (int k = 0; k < myLessonDetails.size(); k++) {
                String startTimeString = null;
                String endTimeString = null;
                for (int j = 0; j < myLessonDetails.get(k).length; j++) {
                    switch (j) {
                        case 0:
                            startTimeString = myLessonDetails.get(k)[j].substring(0, 2) + ":" + myLessonDetails.get(k)[j].substring(2);
                            break;
                        case 1:
                            endTimeString = myLessonDetails.get(k)[j].substring(0, 2) + ":" + myLessonDetails.get(k)[j].substring(2);
                            break;
                        case 2:
                            startDay = getNumberFromDay(myLessonDetails.get(k)[j]);
                            endDay = startDay;
                            break;
                        case 3:
                            //future improvement: since myLessonDetails.get(k)[3] contains data on the
                            // week number that this class occurs on, add capability of schedule to reflect
                            // schedule of the current week.
                            break;
                        default:
                            //data only has four sections from api
                            break;
                    }
                }
                member.addBusyBlocks(name, startDay, startTimeString, endDay, endTimeString);
            }
            TextUI.showAddedMember(member.getName());
        } catch (InvalidUrlException e) {
            System.out.println(e.getMessage());
        }
        return member;
    }

    private static Integer getNumberFromDay(String day) {
        int dayInNumber;
        switch (day) {
            case "Monday":
                dayInNumber = 1;
                break;
            case "Tuesday":
                dayInNumber = 2;
                break;
            case "Wednesday":
                dayInNumber = 3;
                break;
            case "Thursday":
                dayInNumber = 4;
                break;
            case "Friday":
                dayInNumber = 5;
                break;
            case "Saturday":
                dayInNumber = 6;
                break;
            case "Sunday":
                dayInNumber = 0;
                break;
            default:
                dayInNumber = Integer.parseInt(null);
                break;
        }
        return dayInNumber;
    }
    private static int check(TeamMember person, String name) {
        if (person.getName().equals(name)) {
            return 1;
        } else {
            return 0;
        }
    }
    public static void listMeetings(MeetingList meetingList) {
        TextUI.listMeetings();
        meetingList.show();
    }

    public static void deleteMeeting(String userInputWord, MeetingList meetingList, TeamMember mainUser, TeamMemberList
            teamMemberList) {
        int index = Integer.parseInt(userInputWord) - 1;
        try {
            Meeting meetingToDelete = meetingList.getMeetingList().get(index);
            String meetingNameToDelete = meetingToDelete.getMeetingName();
            mainUser.deleteBlocksWithName(meetingNameToDelete);
            meetingList.delete(index);
            teamMemberList.set(0, mainUser);
        } catch (IndexOutOfBoundsException e) {
            TextUI.displayInvalidDeleteTarget();
        }
    }

    public static void scheduleMeeting(String[] userInputWords, MeetingList meetingList, TeamMember mainUser,
                                       TeamMemberList teamMemberList) {
        Integer startDay;
        Integer endDay;
        String meetingName = userInputWords[1];
        startDay = Integer.parseInt(userInputWords[2]);
        LocalTime startTime = LocalTime.parse(userInputWords[3]);
        endDay = Integer.parseInt(userInputWords[4]);
        LocalTime endTime = LocalTime.parse(userInputWords[5]);

        try {
            if (ScheduleHandler.isValidMeeting(mainUser, startDay, startTime, endDay, endTime)) {
                Meeting myMeeting = new Meeting(meetingName, startDay, startTime, endDay, endTime);
                meetingList.add(myMeeting);
                mainUser.addBusyBlocks(meetingName, startDay, userInputWords[3], endDay, userInputWords[5]);
                TextUI.meetingListSizeMsg(meetingList);
            } else {
                System.out.println("Schedule is blocked at that timeslot");
            }
        } catch (MoException e) {
            System.out.println(e.getMessage() + ", try again.");
        }
        // Replace main user's timetable with updated meeting blocks into TeamMember.TeamMemberList for storage purposes.
        teamMemberList.set(0, mainUser);
    }

    public static void displayTimetable(String[] userInputWords, TeamMember mainUser, TeamMemberList teamMemberList) {
        int memberNumber;
        TeamMember member;
        try {
            if (userInputWords.length > 1) {
                ArrayList<TeamMember> myScheduleList = new ArrayList<TeamMember>();
                for (int i = 1; i < userInputWords.length; i++) {
                    memberNumber = Integer.parseInt(userInputWords[i]);
                    member = teamMemberList.getTeamMemberList().get(memberNumber);
                    myScheduleList.add(member);
                }
                ScheduleHandler myScheduleHandler = new ScheduleHandler(myScheduleList);
                Boolean[][] myMasterSchedule;
                myMasterSchedule = myScheduleHandler.getMasterSchedule();
                System.out.println("Timetable of the selected team member/s:");
                TextUI.printTimetable(myMasterSchedule);
            } else {
                System.out.println("Your timetable:");
                TextUI.printTimetable(mainUser.getSchedule());
            }
        } catch (IndexOutOfBoundsException e) {
            TextUI.indexOutOfBoundsMsg();
            System.out.println("Index should be within range 0 to " + (teamMemberList.getTeamMemberList().size() - 1) + ".");
            listContacts(teamMemberList, mainUser);
        } catch (NumberFormatException e) {
            TextUI.invalidNumberTimetableMsg();
        }
    }

    public static void listContacts(TeamMemberList teamMemberList, TeamMember mainUser) {
        TextUI.teamMemberListMsg(teamMemberList.getTeamMemberList(), mainUser.getName());
    }


}