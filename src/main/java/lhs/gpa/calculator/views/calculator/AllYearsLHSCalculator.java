package lhs.gpa.calculator.views.calculator;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.UIScope;
import lhs.gpa.calculator.backend.Class;
import lhs.gpa.calculator.backend.*;
import lhs.gpa.calculator.views.main.MainView;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Route(value = "lhs-calculator/all-years", layout = MainView.class)
@RouteAlias(value = "", layout = MainView.class)
@PWA(name = "LHS Course Profiler", shortName = "LHS GPA",
     description = "Helps in the management of LHS Courses and helps calculate GPA")
@PageTitle("All Years Calculator | GPA Genie")
@UIScope
@PreserveOnRefresh
public class AllYearsLHSCalculator extends VerticalLayout {
    
    private final List<Class> classList    = ClassList.getClassList(this.getClass().getResourceAsStream("/data/class-list.classes"));
    private final List<Grade> gradeChoices = Grade.gradeList();
    
    private final VerticalLayout freshmanYear  = new VerticalLayout();
    private final VerticalLayout sophomoreYear = new VerticalLayout();
    private final VerticalLayout juniorYear    = new VerticalLayout();
    private final VerticalLayout seniorYear    = new VerticalLayout();
    
    private final List<Course> freshmanCourses  = new ArrayList<>();
    private final List<Course> sophomoreCourses = new ArrayList<>();
    private final List<Course> juniorCourses    = new ArrayList<>();
    private final List<Course> seniorCourses    = new ArrayList<>();
    
    private final List<List<Course>>   courseList = Arrays.asList(freshmanCourses, sophomoreCourses, juniorCourses, seniorCourses);
    private final List<VerticalLayout> layoutList = Arrays.asList(freshmanYear, sophomoreYear, juniorYear, seniorYear            );
    
    private int currentCourseYearSelection = 0;
    
    public AllYearsLHSCalculator() {
        setUpYears();
    }
    
    public void setUpYears() {
        for (int i = 0; i < layoutList.size(); i++) {
            VerticalLayout v = layoutList.get(i);
            layoutList.get(i).removeAll();
            String year =  getYear(i);
            
            H1 heading = new H1(year);
            v.add(heading);
            
            HorizontalLayout buttonBar = getMoveButtonBar(i);
            v.add(buttonBar);
    
    
            FormLayout formLayout = new FormLayout();
            formLayout.setResponsiveSteps(
                    new ResponsiveStep("0", 1),
                    new ResponsiveStep("40em", 7)
            );
    
            List<Course> courses = courseList.get(i);
    
            for (int x = 0; x < 10; x++) {
                Course current;
                if (courseList.get(i).size() > x) {
                    current = courses.get(x);
                } else {
                    current = new Course(false);
                    courses.add(current);
                }
    
                ComboBox<Class> classChoices = new ComboBox<>("Class", classList);
                try {
                    classChoices.setValue(current.getaClass());
                } catch (Exception e) {
                    //Ignore it
                }
                classChoices.setClearButtonVisible(true);
    
                ComboBox<Grade> firstSemester = new ComboBox<>("First Semester", gradeChoices);
                try {
                    firstSemester.setValue(current.getFirstSemester());
                } catch (Exception e) {
                    //Ignore it
                }
    
                ComboBox<Grade> secondSemester = new ComboBox<>("Second Semester", gradeChoices);
                try {
                    secondSemester.setValue(current.getSecondSemester());
                } catch (Exception e) {
                    //Do Nothing
                }
    
                ComboBox<Grade> finalsGrade = new ComboBox<>("Finals (If Applicable)", gradeChoices);
                try {
                    finalsGrade.setValue(current.getFinals());
                } catch (Exception e) {
                    //Ignore it
                }
    
                TextField gradeValue = new TextField("Final Grade");
                gradeValue.setReadOnly(true);
                try {
                    gradeValue.setValue(String.format("%s, %s", current.getGrade().toString(), current.getGPA().toString()));
                } catch (Exception e) {
                    //Ignore it
                }
    
                classChoices.addValueChangeListener(onChange -> {
                    try {
                        current.setClass(onChange.getValue());
                        current.setReal(firstSemester.getValue() != null);
                    } catch (Exception e) {
                        //Do Nothing
                    }
                });
    
                firstSemester.addValueChangeListener(grade -> {
                    current.setFirstSemester(grade.getValue());
                    current.setReal(classChoices.getValue() != null);
                    gradeValue.setValue(String.format("%s, %s", current.getGrade().toString(), current.getGPA().toString()));
                });
    
                secondSemester.addValueChangeListener(grade -> {
                    current.setSecondSemester(grade.getValue());
                    gradeValue.setValue(String.format("%s, %s", current.getGrade().toString(), current.getGPA().toString()));
                });
    
                finalsGrade.addValueChangeListener(grade -> {
                    current.setFinals(grade.getValue());
                    gradeValue.setValue(String.format("%s, %s", current.getGrade().toString(), current.getGPA().toString()));
                });
    
                formLayout.add(classChoices, 3);
                formLayout.add(firstSemester, 1);
                formLayout.add(secondSemester, 1);
                formLayout.add(finalsGrade, 1);
                formLayout.add(gradeValue, 1);
            }
            v.add(formLayout);
    
            Button calculateButton = createCalculateButton("Calculate", false);
            Button calculateWithMaxButton = createCalculateButton("Calculate (With Max)", true);
            Button clearButton = createClearButton();
            Button saveButton = saveButton();
            Upload upload = uploadField();
    
            HorizontalLayout layout = new HorizontalLayout(calculateButton, calculateWithMaxButton, clearButton);

            v.add(getMoveButtonBar(i), layout, saveButton, upload);
            v.setHorizontalComponentAlignment(Alignment.CENTER, heading, buttonBar, layout, saveButton, upload);
        }
        
        add(freshmanYear);
    }
    
    public HorizontalLayout getMoveButtonBar(int year) {
        HorizontalLayout buttonBar = new HorizontalLayout();
        
        Button leftButton = new Button(String.format("❮ %s", getYear(year - 1)));
        leftButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        leftButton.addClickListener(buttonClickEvent -> {
            removeAll();
            add(layoutList.get(--currentCourseYearSelection));
        });
        
        Button rightButton = new Button(String.format("%s ❯", getYear(year + 1)));
        rightButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        rightButton.setIconAfterText(true);
        rightButton.addClickListener(buttonClickEvent -> {
            removeAll();
            add(layoutList.get(++currentCourseYearSelection));
        });
        
        if (year == 3) {
            buttonBar.add(leftButton);
        }
        else if (year == 0) {
            buttonBar.add(rightButton);
        }
        else {
            buttonBar.add(leftButton, rightButton);
        }
        
        buttonBar.setAlignItems(Alignment.CENTER);
        buttonBar.setJustifyContentMode(JustifyContentMode.CENTER);
        buttonBar.setAlignSelf(Alignment.CENTER);
        buttonBar.setSizeFull();
        
        return buttonBar;
    }
    
    public Button createCalculateButton(String buttonName, boolean withMax) {
        Button calculate = new Button(buttonName);
        calculate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        calculate.addClickListener(buttonClickEvent -> {
            //Create the Notification For GPA
            Notification gpaNotify = new Notification();
            gpaNotify.setPosition(Notification.Position.MIDDLE);
            
            VerticalLayout GPAInfo = new VerticalLayout();
            
            MultiYearCourseList courseList = new MultiYearCourseList(this.courseList);
            GPAValue   gpaAll     = courseList.getAllCourseGPA();
            GPAValue   gpaAllMax  = courseList.getMaxAllCourseGPA();
            GPAValue   gpaCore    = courseList.getCoreGPA();
            GPAValue   gpaCoreMax = courseList.getMaxCoreGPA();
            GPAValue   gpaUW      = courseList.getUnweightedGPA();
            GPAValue   gpaUWMax   = courseList.getMaxUnweightedGPA();
            
            VerticalLayout gpa;
            
            if (withMax) {
                GPAInfo.add(new H2("Calculated GPA: Current/Max"));
                
                gpa = new VerticalLayout(
                        new Html(String.format("<p style='font-size:150%%'>%s/%s</p>", gpaAll.toString(), gpaAllMax.toString())),
                        new Html(String.format("<p style='font-size:150%%'>%s/%s</p>", gpaCore.toString(), gpaCoreMax.toString())),
                        new Html(String.format("<p style='font-size:150%%'>%s/%s</p>", gpaUW.toString(), gpaUWMax.toString()))
                );
            } else {
                GPAInfo.add(new H2("Calculated GPA"));
                
                gpa = new VerticalLayout(
                        new Html(String.format("<p style='font-size:150%%'>%s</p>", gpaAll.toString())),
                        new Html(String.format("<p style='font-size:150%%'>%s</p>", gpaCore.toString())),
                        new Html(String.format("<p style='font-size:150%%'>%s</p>", gpaUW.toString()))
                );
            }
            
            gpa.setWidth("25%");
            
            VerticalLayout gpaLevels = new VerticalLayout(
                    new Html("<p style='font-size:150%'><strong>Weighted All Course GPA: </strong></p>"),
                    new Html("<p style='font-size:150%'><strong>Weighted Core GPA: </strong></p>"),
                    new Html("<p style='font-size:150%'><strong>Unweighted GPA: </strong></p>")
            );
            
            gpaLevels.setWidth("23em");
            
            
            HorizontalLayout gpaValues = new HorizontalLayout(gpaLevels, gpa);
            gpaValues.setSpacing(false);
            gpaValues.setJustifyContentMode(JustifyContentMode.CENTER);
            gpaValues.setSizeFull();
            
            GPAInfo.add(gpaValues);
            
            GPAInfo.setSizeFull();
            
            GPAInfo.setJustifyContentMode(JustifyContentMode.CENTER);
            GPAInfo.setAlignItems(Alignment.CENTER);
            
            Button closeNotification = new Button("Close", buttonClickEvent1 -> gpaNotify.close());
            closeNotification.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            
            GPAInfo.add(closeNotification);
            
            gpaNotify.add(GPAInfo);
            
            gpaNotify.open();
        });
        
        calculate.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        return calculate;
    }
    
    public Button createClearButton() {
        Button clearButton = new Button("Clear", buttonClickEvent -> {
            freshmanCourses.clear();
            sophomoreCourses.clear();
            juniorCourses.clear();
            seniorCourses.clear();
            removeAll();
            setUpYears();
        });
        
        clearButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        
        return clearButton;
    }
    
    public Upload uploadField() {
        MemoryBuffer buffer = new MemoryBuffer();
        Upload uploadData = new Upload(buffer);
        uploadData.setDropLabel(new Span("Upload Data"));
    
        uploadData.addSucceededListener(event -> {
            Notification decrypt = new Notification();
            PasswordField password = new PasswordField("Encryption Key");
            password.setPlaceholder("Password");
    
            Button decryptButton = new Button("Decrypt", VaadinIcon.KEY.create());
            decryptButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            decryptButton.addClickListener(onClick -> {
                try (InputStream decryptedFile = CryptoUtils.decryptFile(buffer.getInputStream(), password.getValue())) {
                    CourseList.setContent(courseList, decryptedFile);
                    setUpYears();
                    decrypt.close();
                } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException
                        | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | IllegalArgumentException e) {
                    e.printStackTrace();
                    Notification fail = new Notification();
                    fail.setText("Incorrect Password");
                    fail.setDuration(5000);
                    fail.addThemeVariants(NotificationVariant.LUMO_ERROR);
                    fail.open();
                    password.setValue("");
                }
            });
            
            decryptButton.addClickShortcut(Key.ENTER);
            decryptButton.addClickShortcut(Key.NUMPAD_ENTER);
    
            Button cancelButton = new Button("Cancel", onclick -> {
                decrypt.close();
            });
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    
            HorizontalLayout buttons = new HorizontalLayout(decryptButton, cancelButton);
            buttons.setWidthFull();
            buttons.setAlignSelf(Alignment.CENTER);
            buttons.setAlignItems(Alignment.CENTER);
    
            VerticalLayout passwordCase = new VerticalLayout(password, buttons);
            passwordCase.setAlignItems(Alignment.CENTER);
            passwordCase.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            passwordCase.setWidthFull();
    
            decrypt.add(passwordCase);
            decrypt.setPosition(Notification.Position.MIDDLE);
            decrypt.open();
        });
        
        return uploadData;
    }
    
    public Button saveButton() {
        Button saveButton = new Button("Save", VaadinIcon.DOWNLOAD_ALT.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        
        saveButton.addClickListener(buttonClickEvent -> {
            Notification getPassword = new Notification();
            
            PasswordField password = new PasswordField("Encryption Key");
            password.setPlaceholder("Password");
    
            Anchor anchor = new Anchor(new StreamResource("gpa-calc-data.enc", () -> makeInputStreamOfContent(password.getValue())), "");
            anchor.getElement().setAttribute("download", true);
    
            Button anchorButton = new Button("Export", VaadinIcon.DOWNLOAD_ALT.create());
            anchorButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            anchorButton.addClickListener(onClick -> getPassword.close());
    
            anchor.add(anchorButton);
            
            Button cancelButton = new Button("Cancel", onclick -> getPassword.close());
            cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            
            HorizontalLayout buttons = new HorizontalLayout(anchor, cancelButton);
            buttons.setWidthFull();
            buttons.setAlignSelf(Alignment.CENTER);
            buttons.setAlignItems(Alignment.CENTER);
            
            VerticalLayout passwordCase = new VerticalLayout(password, buttons);
            passwordCase.setAlignItems(Alignment.CENTER);
            passwordCase.setDefaultHorizontalComponentAlignment(Alignment.CENTER);
            passwordCase.setWidthFull();
            
            getPassword.add(passwordCase);
            getPassword.setPosition(Notification.Position.MIDDLE);
            getPassword.open();
        });
        
        return saveButton;
    }
    
    private InputStream makeInputStreamOfContent(String password) {
        StringBuilder fileString = new StringBuilder();
        
        for (List<Course> courses : courseList) {
            int number = 0;
            StringBuilder courseString = new StringBuilder();
            for (Course course : courses) {
                if (course.isReal()) {
                    number++;
                    courseString.append(course.toStringExport()).append("\n");
                    System.out.println("Appending");
                }
            }
            fileString.append(number).append("\n");
            fileString.append(courseString);
        }
        
        System.out.println(fileString.toString());
        
        InputStream in = new ByteArrayInputStream(fileString.toString().getBytes(StandardCharsets.UTF_8));
        OutputStream out = null;
        
        InputStream toReturn = null;
        try {
            out = CryptoUtils.encryptFile(in, password);
            toReturn = new ByteArrayInputStream(((ByteArrayOutputStream) out).toByteArray());
            System.out.println(toReturn.toString());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | InvalidAlgorithmParameterException |
                InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        
        return toReturn;
    }
    
    public String getYear(int year) {
        switch(year) {
            case -1:
            case 0:
                return "Freshman Year";
            case 1:
                return "Sophomore Year";
            case 2:
                return "Junior Year";
            default:
                return "Senior Year";
        }
    }
}
