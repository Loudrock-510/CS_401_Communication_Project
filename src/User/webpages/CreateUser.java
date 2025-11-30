package User.webpages;

import java.awt.*;
import javax.swing.*;

class CreateUser extends JPanel {
    private final TeamChatApp app;

    CreateUser(TeamChatApp app) {
        this.app = app;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(24));

        JLabel title = new JLabel("Create a New User");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(title);

        add(Box.createVerticalStrut(16));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField username = new JTextField();
        username.setMaximumSize(new Dimension(400, 40));
        new TextPrompt("Username", username);

        JButton submit = new JButton("Submit");
        submit.setMaximumSize(new Dimension(200, 40));

        JButton cancel = new JButton("Cancel");
        cancel.setMaximumSize(new Dimension(200, 40));

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.add(submit);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(cancel);

        form.add(username);
        form.add(Box.createVerticalStrut(12));
        form.add(buttons);
        form.add(Box.createVerticalGlue());

        add(form);
        add(Box.createVerticalGlue());

        submit.addActionListener(e -> {
            String u = username.getText().trim();
            if (u.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username.");
            } else {
                JOptionPane.showMessageDialog(this, "Created user: " + u + ".\n(Default password: " + " assigned.)");
                //implement user creation logic here, are we randomizing passwords?
                //What if username already exists?
                //User newUser = new User(u, generateRandomPassword());
                app.showSearchIT();
            }
        });
        cancel.addActionListener(e -> app.showSearchIT());
    }
}
