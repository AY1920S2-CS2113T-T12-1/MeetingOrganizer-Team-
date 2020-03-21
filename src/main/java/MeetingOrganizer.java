import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * TESTING SUMMARY DOC.
 */
public class MeetingOrganizer {
    public static Storage storage;
    private MeetingList myMeetingList;
    private TeamMemberList myTeamMemberList;

    public MeetingOrganizer() {
        //declare objects here
        myMeetingList = new MeetingList();
        myTeamMemberList = new TeamMemberList(new ArrayList<TeamMember>());
        try {
            storage = new Storage("data/meeting_list.txt");
            myMeetingList = new MeetingList(storage.loadListFromDisk());
        } catch (FileNotFoundException e) {
            TextUI.showLoadingError();
            myMeetingList = new MeetingList();
        }
    }

    public static void main(String[] args) throws InvalidUrlException {
        new MeetingOrganizer().run();
        //new MeetingOrganizer().generateIndividualLesson("");

    }

    void botResponse(String[] userInputWords, Scanner in) throws MoException, DateTimeParseException, NumberFormatException {
        String userCommand = userInputWords[0];
        Integer startDay;
        Integer endDay;
        TeamMember myMember;
        switch (userCommand) {
        case "add": // add memberName startDay startTime endDay endTime (eg. add xizhi 2 02:00 3 14:00)
            myMember = new TeamMember(userInputWords[1]);
            String scheduleName = userInputWords[1]; //member name and schedule name are the same
            startDay = Integer.parseInt(userInputWords[2]);
            String startTimeString = userInputWords[3];
            endDay = Integer.parseInt(userInputWords[4]);
            String endTimeString = userInputWords[5];
            myMember.addBusyBlocks(scheduleName, startDay, startTimeString, endDay, endTimeString);

            myTeamMemberList.add(myMember);
            break;
        case "contacts":  // contacts
            TextUI.teamMemberListMsg(myTeamMemberList.getTeamMemberList());
            break;
        case "schedule": // schedule memberNumber1 memberNumber2 (eg. schedule 1 3)
            ArrayList<TeamMember> myScheduleList = new ArrayList<TeamMember>();
            for (int i = 1; i < userInputWords.length; i++) {
                int memberNumber = Integer.parseInt(userInputWords[i]);
                myMember = myTeamMemberList.getTeamMemberList().get(memberNumber - 1);
                myScheduleList.add(myMember);

                System.out.println(myMember.getName() + " schedule: ");
                TextUI.printTimetable(myMember.getSchedule());
            }

            ScheduleHandler myScheduleHandler = new ScheduleHandler(myScheduleList);

            Boolean[][] myMasterSchedule = new Boolean[7][48];
            myMasterSchedule = myScheduleHandler.getMasterSchedule();
            System.out.println("master schedule BEFORE:");
            TextUI.printTimetable(myMasterSchedule);

            myScheduleHandler.printFreeTimings();
            TextUI.meetingDetailsMsg();

            String userInput = in.nextLine();
            userInputWords = CliParser.splitWords(userInput);

            String meetingName = userInputWords[0];
            startDay = Integer.parseInt(userInputWords[1]);
            LocalTime startTime = LocalTime.parse(userInputWords[2]);
            endDay = Integer.parseInt(userInputWords[3]);
            LocalTime endTime = LocalTime.parse(userInputWords[4]);

            try {
                if (myScheduleHandler.isValidMeeting(startDay, startTime, endDay, endTime)) {
                    myMeetingList.add(new Meeting(meetingName, startDay, startTime, endDay, endTime));
                    myScheduleHandler.updateMasterSchedule(startDay, startTime, endDay, endTime);
                    myMasterSchedule = myScheduleHandler.getMasterSchedule();

                    System.out.println("master schedule AFTER:");
                    TextUI.printTimetable(myMasterSchedule);

                    TextUI.meetingListSizeMsg(myMeetingList);
                } else {
                    System.out.println("schedule is blocked at that timeslot");
                }
            } catch (MoException e) {
                System.out.println(e.getMessage() + ", try again.");
            }

            break;
        case "2":
            TextUI.editMeetingMsg();

            break;
        case "3":
            TextUI.deleteMeetingMsg();
            int index = Integer.parseInt(String.valueOf(in.next())) - 1;
            try {
                myMeetingList.delete(index);
            } catch (IndexOutOfBoundsException e) {
                TextUI.displayInvalidDeleteTarget();
                TextUI.menuMsg();
            }
            break;
        case "4": //list all current meeting slots
            TextUI.listMeetings();
            myMeetingList.show();
            break;
        default:
            throw new MoException("Unknown command, please try again.");
        }
    }


    private void setMembersSchedule(Scanner in) {
        TextUI.membersMsg();

        //TODO handle exception if user doesn't input integer or input too many members.
        Integer membersN = Integer.parseInt(in.nextLine());
        for (int i = 0; i < membersN; ++i) {
            String addBlocksSuccessOrNot = "";
            TeamMember member = new TeamMember(String.valueOf(i)); //TODO change to member's name.
            do {
                System.out.println(addBlocksSuccessOrNot);
                TextUI.enterScheduleMsg(String.valueOf(i + 1));
                String input = in.nextLine();
                String[] scheduleDetails = input.split(" ", 5);
                String scheduleName = scheduleDetails[0];
                Integer startDay = Integer.parseInt(scheduleDetails[1]);
                String startTime = scheduleDetails[2];
                Integer endDay = Integer.parseInt(scheduleDetails[3]);
                String endTime = scheduleDetails[4];
                addBlocksSuccessOrNot = member.addBusyBlocks(scheduleName, startDay, startTime, endDay, endTime);
            } while (!addBlocksSuccessOrNot.equals("SUCCESS"));
            myScheduleList.add(member);
        }
        myScheduleHandler = new ScheduleHandler(myScheduleList);
        myMasterSchedule = myScheduleHandler.getMasterSchedule();
        TextUI.printTimetable(myMasterSchedule);
        myScheduleHandler.printFreeTimings();
    }

    public void generateIndividualLesson(String webLink) throws InvalidUrlException {
        LessonsGenerator myLessonGenerator = new LessonsGenerator(webLink);
        myLessonGenerator.generate();
        ArrayList<String[]> myLessonDetails = myLessonGenerator.getLessonDetails();

        for (int k = 0; k < myLessonDetails.size(); k++) {
            for (int j = 0; j < myLessonDetails.get(k).length; j++) {
                System.out.print(myLessonDetails.get(k)[j] + " ");
            }
            System.out.print("\n");
        }
    }


    /**
     * Main entry-point for the application.
     */
    public void run() {
        TextUI.introMsg();
        Scanner in = new Scanner(System.in);
        TextUI.menuMsg();
        String userInput = in.nextLine();

        while (!userInput.equals("exit")) {
            String[] userInputWords = CliParser.splitWords(userInput);
            try {
                botResponse(userInputWords, in);
                storage.updateListToDisk(myMeetingList.getMeetingList());
            } catch (MoException e) {
                TextUI.errorMsg(e);
                TextUI.menuMsg();
            } catch (DateTimeParseException e) {
                TextUI.timeOutOfRangeMsg();
                TextUI.menuMsg();
            } catch (NumberFormatException e) {
                TextUI.invalidNumberMsg();
                TextUI.menuMsg();
            } finally {
                TextUI.menuMsg();
                userInput = in.nextLine();
            }
        }
        TextUI.exitMsg();
    }

}

